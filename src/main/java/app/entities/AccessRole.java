package app.entities;

import app.security.AccessRoleName;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Getter
@Table(name = "roles")
@Entity
@NoArgsConstructor
public class AccessRole {

    @Id
    @Column(name = "role_name", nullable = false, updatable = false)
    @Enumerated(value = EnumType.STRING)
    private AccessRoleName roleName;
    @ManyToMany
    private Set<User> users = new HashSet<>();



    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof AccessRole role)) return false;
        return Objects.equals(roleName, role.roleName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(roleName);
    }
}
