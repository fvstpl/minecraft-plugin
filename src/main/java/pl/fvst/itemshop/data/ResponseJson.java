package pl.fvst.itemshop.data;

import lombok.Data;
import pl.fvst.itemshop.data.sub.Action;
import pl.fvst.itemshop.data.sub.Field;

import java.util.List;

@Data
public class ResponseJson {
    private String id;
    private String sessionId;
    private String type;
    private String price;
    private String shopId;
    private String productId;
    private String status;
    private boolean delivered;
    private List<Field> fields;
    private List<Action> actions;
    private String createdAt;
    private String updatedAt;
}
