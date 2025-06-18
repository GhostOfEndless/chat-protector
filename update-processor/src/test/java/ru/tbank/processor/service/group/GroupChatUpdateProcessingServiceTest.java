package ru.tbank.processor.service.group;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.tbank.common.entity.ChatModerationSettings;
import ru.tbank.common.entity.enums.TextProcessingResult;
import ru.tbank.common.entity.enums.UserRole;
import ru.tbank.common.entity.spam.SpamProtectionSettings;
import ru.tbank.common.entity.text.TextModerationSettings;
import ru.tbank.common.telegram.Chat;
import ru.tbank.common.telegram.GroupMemberEvent;
import ru.tbank.common.telegram.Message;
import ru.tbank.common.telegram.TelegramUpdate;
import ru.tbank.common.telegram.User;
import ru.tbank.common.telegram.enums.ChatType;
import ru.tbank.common.telegram.enums.GroupMemberEventType;
import ru.tbank.common.telegram.enums.UpdateType;
import ru.tbank.processor.entity.ChatUser;
import ru.tbank.processor.generated.tables.records.AppUserRecord;
import ru.tbank.processor.generated.tables.records.GroupChatRecord;
import ru.tbank.processor.service.TelegramClientService;
import ru.tbank.processor.service.group.filter.text.TextFilter;
import ru.tbank.processor.service.moderation.ChatModerationSettingsService;
import ru.tbank.processor.service.moderation.ChatUsersService;
import ru.tbank.processor.service.persistence.AppUserService;
import ru.tbank.processor.service.persistence.GroupChatService;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GroupChatUpdateProcessingServiceTest {

    @Mock
    private TelegramClientService telegramClientService;
    @Mock
    private ChatModerationSettingsService chatModerationSettingsService;
    @Mock
    private ChatUsersService chatUsersService;
    @Mock
    private DeletedMessageReportService reportService;
    @Mock
    private GroupChatService groupChatService;
    @Mock
    private AppUserService appUserService;
    @Mock
    private TextFilter textFilter;

    GroupChatUpdateProcessingService service;

    @BeforeEach
    void setUp() {
        service = new GroupChatUpdateProcessingService(
                telegramClientService,
                chatModerationSettingsService,
                chatUsersService,
                reportService,
                groupChatService,
                appUserService,
                List.of(textFilter)
        );
    }

    @Test
    void testProcess_GroupMemberEvent() {
        Chat chat = new Chat(1L, "group", ChatType.GROUP);
        User user = User.builder().id(123L).build();
        GroupMemberEvent event = new GroupMemberEvent(GroupMemberEventType.GROUP_BOT_ADDED, chat, user);
        TelegramUpdate update = new TelegramUpdate(UpdateType.GROUP_MEMBER_EVENT, null, null, event);

        AppUserRecord owner = new AppUserRecord();
        owner.setRole(UserRole.OWNER.name());
        when(appUserService.findById(user.id())).thenReturn(Optional.of(owner));

        service.process(update);

        verify(groupChatService).save(chat.id(), chat.title());
        verify(chatModerationSettingsService).createChatConfig(chat.id(), chat.title());
    }

    @Test
    void testProcess_GroupMessage() {
        Chat chat = new Chat(2L, "chat", ChatType.GROUP);
        User user = User.builder().id(1L).build();
        Message message = new Message(111, "text", user, chat, List.of());
        TelegramUpdate update = new TelegramUpdate(UpdateType.GROUP_MESSAGE, message, null, null);

        var groupChat = new GroupChatRecord();
        groupChat.setId(chat.id());
        groupChat.setName(chat.title());
        var settings = mock(ChatModerationSettings.class);
        var textSettings = mock(TextModerationSettings.class);
        var spamSettings = mock(SpamProtectionSettings.class);

        when(groupChatService.findById(chat.id())).thenReturn(Optional.of(groupChat));
        when(chatModerationSettingsService.findChatConfigById(chat.id())).thenReturn(Optional.of(settings));
        when(settings.getTextModerationSettings()).thenReturn(textSettings);
        when(settings.getSpamProtectionSettings()).thenReturn(spamSettings);
        when(spamSettings.isEnabled()).thenReturn(false);
        when(textFilter.process(message, textSettings)).thenReturn(TextProcessingResult.OK);

        service.process(update);

        verify(textFilter).process(message, textSettings);
        verify(reportService, never()).saveReport(any(), any());
    }

    @Test
    void testTextFilterTriggered_deletesMessage() {
        Chat chat = new Chat(3L, "chat", ChatType.GROUP);
        User user = User.builder().id(2L).build();
        Message message = new Message(112, "bad", user, chat, List.of());

        var settings = mock(ChatModerationSettings.class);
        var textSettings = mock(TextModerationSettings.class);
        var spamSettings = mock(SpamProtectionSettings.class);

        when(settings.getTextModerationSettings()).thenReturn(textSettings);
        when(settings.getSpamProtectionSettings()).thenReturn(spamSettings);
        when(spamSettings.isEnabled()).thenReturn(false);
        when(textFilter.process(message, textSettings)).thenReturn(TextProcessingResult.LINK_FOUND);

        service.processTextMessage(message, settings);

        verify(telegramClientService).deleteMessage(chat.id(), message.messageId());
        verify(reportService).saveReport(message, TextProcessingResult.LINK_FOUND);
    }

    @Test
    void testSpamProtection_userWithinCooldown_messageDeleted() {
        Chat chat = new Chat(4L, "chat", ChatType.GROUP);
        long userId = 10L;
        User user = User.builder().id(userId).build();
        Message message = new Message(201, "spam", user, chat, List.of());

        var spamSettings = mock(SpamProtectionSettings.class);
        when(spamSettings.getCoolDownPeriod()).thenReturn(30L);
        when(spamSettings.getExclusions()).thenReturn(Set.of());

        ChatUser chatUser = ChatUser.builder()
                .chatId(chat.id())
                .userId(userId)
                .lastProcessedMessageDt(OffsetDateTime.now(ZoneOffset.UTC).minusSeconds(10))
                .build();

        when(chatUsersService.findChatUser(chat.id(), userId)).thenReturn(Optional.of(chatUser));

        service.processSpamProtection(message, spamSettings);

        verify(telegramClientService).deleteMessage(chat.id(), message.messageId());
        verify(reportService).saveSpamReport(message);
    }

    @Test
    void testSpamProtection_userOutsideCooldown_updated() {
        Chat chat = new Chat(5L, "chat", ChatType.GROUP);
        long userId = 11L;
        User user = User.builder().id(userId).build();
        Message message = new Message(202, "ok", user, chat, List.of());

        var spamSettings = mock(SpamProtectionSettings.class);
        when(spamSettings.getCoolDownPeriod()).thenReturn(10L);
        when(spamSettings.getExclusions()).thenReturn(Set.of());

        ChatUser chatUser = ChatUser.builder()
                .chatId(chat.id())
                .userId(userId)
                .lastProcessedMessageDt(OffsetDateTime.now(ZoneOffset.UTC).minusSeconds(60))
                .build();

        when(chatUsersService.findChatUser(chat.id(), userId)).thenReturn(Optional.of(chatUser));

        service.processSpamProtection(message, spamSettings);

        verify(chatUsersService).updateChatUser(any(ChatUser.class));
    }

    @Test
    void testSpamProtection_newUser_createdAndUpdated() {
        Chat chat = new Chat(6L, "chat", ChatType.GROUP);
        long userId = 12L;
        User user = User.builder().id(userId).build();
        Message message = new Message(203, "new user", user, chat, List.of());

        var spamSettings = mock(SpamProtectionSettings.class);
        when(spamSettings.getExclusions()).thenReturn(Set.of());

        when(chatUsersService.findChatUser(chat.id(), userId)).thenReturn(Optional.empty());

        service.processSpamProtection(message, spamSettings);

        verify(chatUsersService).updateChatUser(argThat(u ->
                u.getUserId().equals(userId) &&
                        u.getChatId().equals(chat.id())
        ));
    }
}
