package bezbednost.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Admin extends BaseEntity{

    @Column(unique = true)
    private String email;

    @Column(nullable = false)
    private String password;
}
