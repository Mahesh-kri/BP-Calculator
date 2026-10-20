package com.example.bpcalculator.controller;

import com.example.bpcalculator.model.BloodPressure;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class BloodPressureController {

    private final MessageSource messageSource;

    // treat blank numeric inputs as null so @NotNull produces a friendly "required" message
    @InitBinder("bp")
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(Integer.class, new CustomNumberEditor(Integer.class, true));
    }

    @GetMapping("/")
    public String index(Model model) {
        BloodPressure bp = new BloodPressure();
        bp.setSystolic(100);
        bp.setDiastolic(60);
        model.addAttribute("bp", bp);
        model.addAttribute("hasErrors", false);
        return "index";
    }

    @PostMapping("/")
    public String index(@Valid @ModelAttribute("bp") BloodPressure bp, BindingResult bindingResult, Model model) {
        // extra validation: only when both values bound successfully
        if (bp.getSystolic() != null && bp.getDiastolic() != null
                && bp.getSystolic() <= bp.getDiastolic()) {
            String message = messageSource.getMessage("bp.error.systolicGreaterThanDiastolic",
                    null, LocaleContextHolder.getLocale());
            bindingResult.reject("", message);
        }
        model.addAttribute("hasErrors", bindingResult.hasErrors());
        return "index";
    }
}
