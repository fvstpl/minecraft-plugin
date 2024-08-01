package pl.fvst.itemshop.data.sub;

import lombok.Data;

@Data
public class Action {
    private String id;
    private String type;
    private String command;
    private String serverId;
}
