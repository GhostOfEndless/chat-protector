package ru.tbank.adminpanel.ui.views;

import com.vaadin.componentfactory.ToggleButton;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.slf4j.Slf4j;
import ru.tbank.adminpanel.service.ChatConfigService;
import ru.tbank.adminpanel.ui.MainLayout;
import ru.tbank.common.entity.ChatConfig;

import java.util.Objects;

@Slf4j
@PageTitle("Текстовые сообщения")
@Route(value = "settings/:chatID/text-moderation", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class TextModerationSettingsView extends Composite<VerticalLayout> implements BeforeEnterObserver {

    private final ChatConfigService chatConfigService;
    private Long chatID;

    public TextModerationSettingsView(ChatConfigService chatConfigService) {
        this.chatConfigService = chatConfigService;

        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        chatID = Long.parseLong(event.getRouteParameters().get("chatID").orElse("0"));
        var chatConfig = chatConfigService.getChatConfig(chatID);

        if (Objects.isNull(chatConfig)) {
            log.warn("Chat ID is incorrect!");
            // TODO: Добавить обработку несуществующего id
        } else {
            buildPage(chatConfig);
        }
    }

    private void buildPage(ChatConfig chatConfig) {
        var layoutColumn = new VerticalLayout();

        layoutColumn.setWidthFull();
        layoutColumn.setWidth("100%");
        layoutColumn.setHeight("min-content");

        layoutColumn.add(createBlockURLsToggle(chatConfig.isBlockLinks()));
        layoutColumn.add(createBlockTagsToggle(chatConfig.isBlockTags()));

        getContent().setFlexGrow(1.0, layoutColumn);
        getContent().add(layoutColumn);
    }

    private ToggleButton createBlockURLsToggle(boolean currentState) {
        var toggleBlockURLs = new ToggleButton("Запрет ссылок");
        toggleBlockURLs.setValue(currentState);
        toggleBlockURLs.addValueChangeListener(evt -> {
            var chatConfig = chatConfigService.getChatConfig(chatID);
            chatConfig.setBlockLinks(evt.getValue());
            chatConfigService.updateChatConfig(chatConfig);
            log.info("URL moderation now is: {}", evt.getValue());
        });

        return toggleBlockURLs;
    }

    private ToggleButton createBlockTagsToggle(boolean currentState) {
        var toggleBlockTags = new ToggleButton("Запрет тегов");
        toggleBlockTags.setValue(currentState);
        toggleBlockTags.addValueChangeListener(evt -> {
            var chatConfig = chatConfigService.getChatConfig(chatID);
            chatConfig.setBlockTags(evt.getValue());
            chatConfigService.updateChatConfig(chatConfig);
            log.info("Tags moderation now is: {}", evt.getValue());
        });

        return toggleBlockTags;
    }
}