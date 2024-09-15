package org.example.adminpanel.ui.views;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import jakarta.annotation.security.RolesAllowed;
import org.example.adminpanel.ui.MainLayout;

@PageTitle("Статистика")
@Route(value = "", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
@RolesAllowed("USER")
public class DashboardView extends Composite<VerticalLayout> {

    public DashboardView() {
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
    }
}