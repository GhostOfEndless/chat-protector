package ru.tbank.adminpanel.ui.views;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import lombok.extern.slf4j.Slf4j;
import ru.tbank.adminpanel.ui.MainLayout;

@Slf4j
@PageTitle("Домашняя страница")
@Route(value = "", layout = MainLayout.class)
@PermitAll
public class HomeView extends Composite<VerticalLayout> {

    public HomeView() {
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
    }
}
