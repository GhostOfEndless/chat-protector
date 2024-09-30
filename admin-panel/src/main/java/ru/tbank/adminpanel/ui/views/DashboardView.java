package ru.tbank.adminpanel.ui.views;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import lombok.extern.slf4j.Slf4j;
import ru.tbank.adminpanel.service.ChatConfigService;
import ru.tbank.adminpanel.ui.MainLayout;
import ru.tbank.common.entity.ChatConfig;

import java.util.Objects;

@Slf4j
@PageTitle("Статистика")
@Route(value = "statistics/:chatID/dashboard", layout = MainLayout.class)
@PermitAll
public class DashboardView extends Composite<VerticalLayout> implements BeforeEnterObserver {

    private final ChatConfigService chatConfigService;
    private Long chatID;

    public DashboardView(ChatConfigService chatConfigService) {
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
        log.info("Dashboard for chat id {} building...", chatID);
    }
}