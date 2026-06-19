package com.im.chat.infrastructure.repository;

import com.im.chat.domain.common.ConversationId;
import com.im.chat.domain.common.UserId;
import com.im.chat.domain.conversation.*;
import com.im.chat.infrastructure.persistence.po.ConversationMemberPO;
import com.im.chat.infrastructure.persistence.po.ConversationPO;
import com.im.chat.infrastructure.persistence.repository.JpaConversationMemberRepository;
import com.im.chat.infrastructure.persistence.repository.JpaConversationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ConversationRepositoryImpl implements ConversationRepository {

    private final JpaConversationRepository convJpaRepo;
    private final JpaConversationMemberRepository memberJpaRepo;

    @Override
    public Optional<Conversation> findById(ConversationId id) {
        return convJpaRepo.findById(id.getValue()).map(po -> {
            List<ConversationMemberPO> memberPOs = memberJpaRepo.findByConversationId(id.getValue());
            return toDomain(po, memberPOs);
        });
    }

    @Override
    public List<Conversation> findByUserId(UserId userId) {
        List<ConversationMemberPO> memberships = memberJpaRepo.findByUserId(userId.getValue());
        Set<String> convIds = memberships.stream()
                .map(ConversationMemberPO::getConversationId)
                .collect(Collectors.toSet());

        List<ConversationPO> convPOs = convJpaRepo.findAllById(convIds);
        Map<String, List<ConversationMemberPO>> memberMap = new HashMap<>();
        for (ConversationMemberPO m : memberJpaRepo.findByConversationIdIn(new ArrayList<>(convIds))) {
            memberMap.computeIfAbsent(m.getConversationId(), k -> new ArrayList<>()).add(m);
        }

        List<Conversation> result = new ArrayList<>();
        for (ConversationPO po : convPOs) {
            result.add(toDomain(po, memberMap.getOrDefault(po.getConversationId(), Collections.emptyList())));
        }
        return result;
    }

    @Override
    public Optional<Conversation> findPrivateConversation(UserId userA, UserId userB) {
        // Find all conversations userA is in, then check for PRIVATE with userB
        List<ConversationMemberPO> memberships = memberJpaRepo.findByUserId(userA.getValue());
        for (ConversationMemberPO m : memberships) {
            Optional<ConversationPO> convOpt = convJpaRepo.findById(m.getConversationId());
            if (convOpt.isPresent() && "PRIVATE".equals(convOpt.get().getType())) {
                List<ConversationMemberPO> allMembers = memberJpaRepo.findByConversationId(m.getConversationId());
                boolean hasUserB = allMembers.stream().anyMatch(mp -> mp.getUserId().equals(userB.getValue()));
                if (hasUserB) {
                    return Optional.of(toDomain(convOpt.get(), allMembers));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    @Transactional
    public Conversation save(Conversation conversation) {
        ConversationPO convPO = toConvPO(conversation);
        convJpaRepo.save(convPO);

        // Delete old members and re-insert (simplified — production would do delta update)
        memberJpaRepo.deleteByConversationId(conversation.getConversationId().getValue());
        for (ConversationMember member : conversation.getMembers()) {
            ConversationMemberPO mpo = new ConversationMemberPO();
            mpo.setConversationId(conversation.getConversationId().getValue());
            mpo.setUserId(member.getUserId().getValue());
            mpo.setRole(member.getRole().name());
            mpo.setJoinedAt(member.getJoinedAt());
            memberJpaRepo.save(mpo);
        }

        // Re-load fresh state
        return findById(conversation.getConversationId())
                .orElseThrow(() -> new RuntimeException("Failed to re-load conversation after save"));
    }

    @Override
    @Transactional
    public void delete(Conversation conversation) {
        memberJpaRepo.deleteByConversationId(conversation.getConversationId().getValue());
        convJpaRepo.deleteById(conversation.getConversationId().getValue());
    }

    private Conversation toDomain(ConversationPO po, List<ConversationMemberPO> memberPOs) {
        Conversation c = new Conversation();
        c.setConversationId(ConversationId.of(po.getConversationId()));
        c.setType(ConversationType.valueOf(po.getType()));
        c.setName(po.getName());
        c.setAvatar(po.getAvatar());
        Set<ConversationMember> members = new LinkedHashSet<>();
        for (ConversationMemberPO mpo : memberPOs) {
            members.add(new ConversationMember(
                    UserId.of(mpo.getUserId()),
                    MemberRole.valueOf(mpo.getRole()),
                    mpo.getJoinedAt()));
        }
        c.setMembers(members);
        c.setCreatedAt(po.getCreatedAt());
        c.setUpdatedAt(po.getUpdatedAt());
        return c;
    }

    private ConversationPO toConvPO(Conversation c) {
        ConversationPO po = new ConversationPO();
        po.setConversationId(c.getConversationId().getValue());
        po.setType(c.getType().name());
        po.setName(c.getName());
        po.setAvatar(c.getAvatar());
        po.setCreatedAt(c.getCreatedAt());
        po.setUpdatedAt(c.getUpdatedAt());
        return po;
    }
}
