package lk.acpt.smartbiz.service;

import lk.acpt.smartbiz.dto.ItemDto;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public interface ItemService {
    ItemDto saveItem(ItemDto item);
    ItemDto deleteItem(Long id);
    List<ItemDto> getAllItems();
    ItemDto updateItem(ItemDto item);
    ItemDto getItemById(Long id);
    ItemDto getItemByName(String name);
}
