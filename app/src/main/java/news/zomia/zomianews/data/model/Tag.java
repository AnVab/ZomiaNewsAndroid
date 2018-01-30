package news.zomia.zomianews.data.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Andrey on 27.01.2018.
 *
 * A class for a DB entity for Tag info
 */

@Entity(indices = {@Index(value = {"tag_id"},
        unique = true)})
public class Tag {

    @PrimaryKey
    @ColumnInfo(name = "tid")
    private Integer tId;

    @ColumnInfo(name = "tag_id")
    private Integer tagId;

    @ColumnInfo(name = "name")
    private String name;

    public Tag(final Integer tagId, final String name) {
        this.tagId = tagId;
        this.name = name;
    }

    public Integer getTId() {
        return tId;
    }

    public void setTId(Integer tId) {
        this.tId = tId;
    }

    public Integer getTagId() {
        return tagId;
    }

    public void setTagId(Integer tagId) {
        this.tagId = tagId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
