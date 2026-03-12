package app.domain.models.person;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Setter
@Getter
@NoArgsConstructor

public abstract class Person {
    private long id;
    private String name;
    private String identificationNumber;
    private String email;
    private String phone;
    private String address;

}
