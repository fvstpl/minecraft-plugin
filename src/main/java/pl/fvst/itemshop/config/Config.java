package pl.fvst.itemshop.config;

import lombok.Data;

@Data
public class Config {

    public Boolean debug;
    public String shopIdentifier;
    public String serverIdentifier;
    public String apiKey;

}
