package dao;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="Tag")
public class Tag implements Serializable {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    @Column(name="oid")
    private long oid;

    @Column(name="tag", unique = true, nullable = false)
    private String tag;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name="img_tags",
            joinColumns = @JoinColumn(name="tag_oid"),
            inverseJoinColumns = @JoinColumn(name="img_oid")
    )
    private Set<ImageId> images = new HashSet<>();

    public Set<ImageId> getImages() {
        return images;
    }

    public void setImages(Set<ImageId> images) {
        this.images = images;
    }

    public long getOid() {
        return oid;
    }

    public void setOid(long oid) {
        this.oid = oid;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
