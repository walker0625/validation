package hello.itemservice.web.validation;

import hello.itemservice.domain.item.Item;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class ItemValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return Item.class.isAssignableFrom(clazz); // item = clazz, item = subItem
    }

    @Override
    public void validate(Object target, Errors bindingResult) {
        Item item = (Item) target;

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

    }

}
