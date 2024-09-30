package ru.tbank.adminpanel.ui;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import com.vaadin.flow.theme.lumo.LumoUtility;
import lombok.extern.slf4j.Slf4j;
import ru.tbank.adminpanel.entity.AppUser;
import ru.tbank.adminpanel.security.AuthenticatedUser;
import ru.tbank.adminpanel.service.ChatConfigService;
import ru.tbank.adminpanel.ui.views.DashboardView;
import ru.tbank.adminpanel.ui.views.HomeView;
import ru.tbank.adminpanel.ui.views.TextModerationSettingsView;

import java.util.Optional;

@Slf4j
public class MainLayout extends AppLayout {

    private H1 viewTitle;

    private final AuthenticatedUser authenticatedUser;
    private final AccessAnnotationChecker accessChecker;
    private final ChatConfigService chatConfigService;

    public MainLayout(AuthenticatedUser authenticatedUser, AccessAnnotationChecker accessChecker,
                      ChatConfigService chatConfigService) {
        this.authenticatedUser = authenticatedUser;
        this.accessChecker = accessChecker;
        this.chatConfigService = chatConfigService;

        setPrimarySection(Section.DRAWER);
        addDrawerContent();
        addHeaderContent();
    }

    private void addHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel("Menu toggle");

        viewTitle = new H1();
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        addToNavbar(true, toggle, viewTitle);
    }

    private void addDrawerContent() {
        Span appName = new Span("Chat Protector");
        appName.addClassNames(LumoUtility.FontWeight.SEMIBOLD, LumoUtility.FontSize.LARGE);
        Header header = new Header(appName);

        Scroller scroller = new Scroller(createNavigation());

        addToDrawer(header, scroller, createFooter());
    }

    private SideNav createNavigation() {
        SideNav nav = new SideNav();
        var configs = chatConfigService.getChatConfigs();
        var homeNav = new SideNavItem("Домашняя страница", HomeView.class, VaadinIcon.HOME.create());
        nav.addItem(homeNav);

        configs.forEach(config -> {
            var sideNavItem = new SideNavItem(config.getChatName(), (String) null, VaadinIcon.CHAT.create());
            var routeParams = new RouteParameters("chatID", String.valueOf(config.getChatId()));

            sideNavItem.addItem(new SideNavItem("Статистика", DashboardView.class, routeParams,
                    VaadinIcon.DASHBOARD.create()));

            if (accessChecker.hasAccess(TextModerationSettingsView.class)) {
                sideNavItem.addItem(new SideNavItem("Текстовые сообщения", TextModerationSettingsView.class,
                        routeParams, VaadinIcon.CLIPBOARD_TEXT.create()));
            }

            nav.addItem(sideNavItem);
        });

        return nav;
    }

    private Footer createFooter() {
        Footer layout = new Footer();

        Optional<AppUser> maybeUser = authenticatedUser.get();
        if (maybeUser.isPresent()) {
            AppUser user = maybeUser.get();

            MenuBar userMenu = new MenuBar();
            userMenu.setThemeName("tertiary-inline contrast");

            MenuItem userName = userMenu.addItem("");
            Div div = new Div();
            div.add(user.getFullName());
            div.add(new Icon("lumo", "dropdown"));
            div.getElement().getStyle().set("display", "flex");
            div.getElement().getStyle().set("align-items", "center");
            div.getElement().getStyle().set("gap", "var(--lumo-space-s)");
            userName.add(div);
            userName.getSubMenu().addItem("Выйти", e -> authenticatedUser.logout());

            layout.add(userMenu);
        } else {
            Anchor loginLink = new Anchor("login", "Войти");
            layout.add(loginLink);
        }

        return layout;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        viewTitle.setText(getCurrentPageTitle());
    }

    private String getCurrentPageTitle() {
        PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
        return title == null ? "" : title.value();
    }
}
