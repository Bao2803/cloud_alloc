package westwood222.cloud_alloc.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.oauth2.client.registration.ClientRegistration;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "account")
public class Account {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "provider", nullable = false)
    @Enumerated(EnumType.STRING)
    private Provider provider;

    @OneToMany(mappedBy = "account")
    private Set<Resource> resources;

    @Column(name = "available_space")
    private Long availableSpace;

    @Transient
    private String accessToken;

    @Column(name = "refresh_token", nullable = false)
    private String refreshToken;

    @Transient
    private ClientRegistration clientRegistration;

    @Transient
    @Column(name = "expiration_time", nullable = false)
    private Long expirationTimeMilliseconds;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
