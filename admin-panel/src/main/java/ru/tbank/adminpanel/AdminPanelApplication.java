package ru.tbank.adminpanel;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Theme(value = "chat-protector")
@PWA(
        name = "Chat Protector | Admin panel",
        shortName = "Chat Protector",
        offlinePath = "offline.html",
        offlineResources = {"images/offline.png"}
)
public class AdminPanelApplication implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(AdminPanelApplication.class, args);
    }
}