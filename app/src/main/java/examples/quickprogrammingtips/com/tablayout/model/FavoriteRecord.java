package examples.quickprogrammingtips.com.tablayout.model;

import com.orm.SugarRecord;
import com.orm.dsl.Unique;

/**
 * Created by anton on 26-1-16.
 */
public class FavoriteRecord extends SugarRecord {
    @Unique
    public String url;
    public String description;
    public String category;
    public FavoriteRecord(){

    }
    public FavoriteRecord(String url,String description,String category){
        this.url=url;
        this.description=description;
        this.category=category;

    }
}
