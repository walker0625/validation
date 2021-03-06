package hello.itemservice.web.validation;

import hello.itemservice.domain.item.Item;
import hello.itemservice.domain.item.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/validation/v2/items")
@RequiredArgsConstructor
public class ValidationItemControllerV2 {

    private final ItemRepository itemRepository;
    private final ItemValidator itemValidator;

    // Controller의 메소드가 호출 될 때마다 호출됨
    @InitBinder
    public void init(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(itemValidator); // Validator - supports()로 어떤 검증기를 사용 할지 결정
    }

    @GetMapping
    public String items(Model model) {
        List<Item> items = itemRepository.findAll();
        model.addAttribute("items", items);
        return "validation/v2/items";
    }

    @GetMapping("/{itemId}")
    public String item(@PathVariable long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v2/item";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("item", new Item());
        return "validation/v2/addForm";
    }

    //@PostMapping("/add") // @ModelAttribute 바로 뒤에 BindingResult가 와야 함
    public String addItemV1(@ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {

        if(!StringUtils.hasText(item.getItemName())) {
            bindingResult.addError(new FieldError("item", "itemName", "상품 이름은 필수입니다."));
        }

        if(item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) {
            bindingResult.addError(new FieldError("item", "price", "가격은 1000~1000000 사이 허용합니다."));
        }

        if(item.getQuantity() == null || item.getQuantity() > 9999) {
            bindingResult.addError(new FieldError("item", "quantity", "수량은 9999이하로 허용합니다."));
        }

        //특정 필드가 아닌 복합 룰 검증
        if (item.getPrice() != null && item.getQuantity() != null) {
            int resultPrice = item.getPrice() * item.getQuantity();
            if (resultPrice < 10000) {
                bindingResult.addError(new ObjectError("item", "가격 * 수량의 합은 10,000원 이상이어야 합니다. 현재 값 = " + resultPrice));
            }
        }
        
        // 검증에 실패하면 다시 정보를 담아서 입력폼으로 > bindingResult는 model에 자동으로 담김
        if(bindingResult.hasErrors()) {
            return "validation/v2/addForm";
        }
        
        // 성공 로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);

        return "redirect:/validation/v2/items/{itemId}";
    }

    //@PostMapping("/add") // @ModelAttribute 바로 뒤에 BindingResult가 와야 함
    public String addItemV2(@ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {

        if(!StringUtils.hasText(item.getItemName())) {
            bindingResult.addError(new FieldError("item", "itemName", item.getItemName(), false,null,null,"상품 이름은 필수입니다."));
        }

        if(item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) {
            bindingResult.addError(new FieldError("item", "price",item.getPrice(), false, null, null, "가격은 1000~1000000 사이 허용합니다."));
        }

        if(item.getQuantity() == null || item.getQuantity() > 9999) {
            bindingResult.addError(new FieldError("item", "quantity", item.getQuantity(), false, null, null, "수량은 9999이하로 허용합니다."));
        }

        //특정 필드가 아닌 복합 룰 검증
        if (item.getPrice() != null && item.getQuantity() != null) {
            int resultPrice = item.getPrice() * item.getQuantity();
            if (resultPrice < 10000) {
                bindingResult.addError(new ObjectError("item", null, null, "가격 * 수량의 합은 10,000원 이상이어야 합니다. 현재 값 = " + resultPrice));
            }
        }

        // 검증에 실패하면 다시 정보를 담아서 입력폼으로 > bindingResult는 model에 자동으로 담김
        if(bindingResult.hasErrors()) {
            return "validation/v2/addForm";
        }

        // 성공 로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);

        return "redirect:/validation/v2/items/{itemId}";
    }

    //@PostMapping("/add") // @ModelAttribute 바로 뒤에 BindingResult가 와야 함 > @ModelAttribute 생략 가능함(받는 객체를 특정하기 위해서는 필요할 듯)
    public String addItemV3(Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {

        if(!StringUtils.hasText(item.getItemName())) {
            bindingResult.addError(new FieldError("item", "itemName", item.getItemName(), false,new String[]{"required.item.itemName"},null,null));
        }

        if(item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) {
            bindingResult.addError(new FieldError("item", "price",item.getPrice(), false, new String[]{"range.item.price"}, new Object[]{1000, 1000000}, null));
        }

        if(item.getQuantity() == null || item.getQuantity() > 9999) {
            bindingResult.addError(new FieldError("item", "quantity", item.getQuantity(), false, new String[]{"max.item.quantity"}, new Object[]{9999}, null));
        }

        //특정 필드가 아닌 복합 룰 검증
        if (item.getPrice() != null && item.getQuantity() != null) {
            int resultPrice = item.getPrice() * item.getQuantity();
            if (resultPrice < 10000) {
                bindingResult.addError(new ObjectError("item", new String[]{"totalPriceMin"}, new Object[]{10000, resultPrice}, null));
            }
        }

        // 검증에 실패하면 다시 정보를 담아서 입력폼으로 > bindingResult는 model에 자동으로 담김
        if(bindingResult.hasErrors()) {
            return "validation/v2/addForm";
        }

        // 성공 로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

    //@PostMapping("/add") // @ModelAttribute 바로 뒤에 BindingResult가 와야 함 > @ModelAttribute 생략 가능함(받는 객체를 특정하기 위해서는 필요할 듯)
    public String addItemV4(Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {

        // 처음에 type 오류로 return 하는 경우는 아래 오류는 포함하지 않기 위해 맨위에서도 처리
        // 검증에 실패하면 다시 정보를 담아서 입력폼으로 > bindingResult는 model에 자동으로 담김
        if(bindingResult.hasErrors()) {
            return "validation/v2/addForm";
        }

        // ValidationUtils.rejectIfEmptyOrWhitespace(bindingResult, "itemName", "required"); > 간단한 공백/empty는  이렇게 한 줄로도 처리 가능
        if(!StringUtils.hasText(item.getItemName())) {
            //bindingResult.addError(new FieldError("item", "itemName", item.getItemName(), false,new String[]{"required.item.itemName"},null,null));
            bindingResult.rejectValue("itemName", "required"); // object.CODE.field
        }

        if(item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) {
            //bindingResult.addError(new FieldError("item", "price",item.getPrice(), false, new String[]{"range.item.price"}, new Object[]{1000, 1000000}, null));
            bindingResult.rejectValue("price", "range", new Object[]{1000, 1000000}, null);
        }

        if(item.getQuantity() == null || item.getQuantity() > 9999) {
            //bindingResult.addError(new FieldError("item", "quantity", item.getQuantity(), false, new String[]{"max.item.quantity"}, new Object[]{9999}, null));
            bindingResult.rejectValue("quantity", "max", new Object[]{9999}, null);
        }

        //특정 필드가 아닌 복합 룰 검증
        if (item.getPrice() != null && item.getQuantity() != null) {
            int resultPrice = item.getPrice() * item.getQuantity();
            if (resultPrice < 10000) {
                //bindingResult.addError(new ObjectError("item", new String[]{"totalPriceMin"}, new Object[]{10000, resultPrice}, null));
                bindingResult.reject("totalPriceMin", new Object[]{10000, resultPrice}, null);
            }
        }

        if(bindingResult.hasErrors()) {
            return "validation/v2/addForm";
        }

        // 성공 로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

    //@PostMapping("/add") // @ModelAttribute 바로 뒤에 BindingResult가 와야 함 > @ModelAttribute 생략 가능함(받는 객체를 특정하기 위해서는 필요할 듯)
    public String addItemV5(Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        
        // 처음에 type 오류로 return 하는 경우는 아래 오류는 포함하지 않기 위해 맨위에서도 처리
        // 검증에 실패하면 다시 정보를 담아서 입력폼으로 > bindingResult는 model에 자동으로 담김
        if(bindingResult.hasErrors()) {
            return "validation/v2/addForm";
        }

        itemValidator.validate(item, bindingResult);

        if(bindingResult.hasErrors()) {
            return "validation/v2/addForm";
        }

        // 성공 로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

    @PostMapping("/add") // @ModelAttribute 바로 뒤에 BindingResult가 와야 함 > @ModelAttribute 생략 가능함(받는 객체를 특정하기 위해서는 필요할 듯)
    public String addItemV6(@Validated Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {

        // 검증에 실패하면 다시 정보를 담아서 입력폼으로 > bindingResult는 model에 자동으로 담김
        if(bindingResult.hasErrors()) {
            return "validation/v2/addForm";
        }

        // 성공 로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

    @GetMapping("/{itemId}/edit")
    public String editForm(@PathVariable Long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v2/editForm";
    }

    @PostMapping("/{itemId}/edit")
    public String edit(@PathVariable Long itemId, @ModelAttribute Item item) {
        itemRepository.update(itemId, item);
        return "redirect:/validation/v2/items/{itemId}";
    }

}

