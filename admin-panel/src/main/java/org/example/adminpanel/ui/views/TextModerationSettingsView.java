package org.example.adminpanel.ui.views;

import com.vaadin.componentfactory.ToggleButton;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.slf4j.Slf4j;
import org.example.adminpanel.ui.MainLayout;

@PageTitle("Текстовые сообщения")
@Route(value = "text-moderation", layout = MainLayout.class)
@RolesAllowed("USER")
@Slf4j
public class TextModerationSettingsView extends Composite<VerticalLayout> {

    public TextModerationSettingsView() {
        HorizontalLayout layoutRow = new HorizontalLayout();
        ToggleButton toggle = new ToggleButton("Модерация текста");
        HorizontalLayout layoutRow2 = new HorizontalLayout();
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
        layoutRow.setWidthFull();
        getContent().setFlexGrow(1.0, layoutRow);
        layoutRow.setWidth("100%");
        layoutRow.setHeight("min-content");
        toggle.addValueChangeListener(evt -> log.info("Checkbox value is {}", evt.getValue()));
        layoutRow2.setWidthFull();
        getContent().setFlexGrow(1.0, layoutRow2);
        layoutRow2.addClassName(LumoUtility.Gap.MEDIUM);
        layoutRow2.setWidth("100%");
        layoutRow2.getStyle().set("flex-grow", "1");
        getContent().add(layoutRow);
        layoutRow.add(toggle);
        getContent().add(layoutRow2);
    }
}