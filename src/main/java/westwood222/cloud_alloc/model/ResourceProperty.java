package westwood222.cloud_alloc.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Data
@Builder
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class ResourceProperty implements Serializable {
    @Column(name = "mine_type", nullable = false)
    private String mineType;

    @Column(name = "name", nullable = false)
    private String name;
}
