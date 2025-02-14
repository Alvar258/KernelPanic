package authn;
import java.io.Serializable;
import jakarta.persistence.*;
import javax.xml.bind.annotation.XmlRootElement; 
import model.entities.Customer;
import authn.JWTUtil;

@Entity
@NamedQuery(name="Credentials.findUser", 
            query="SELECT c FROM Credentials c WHERE c.username = :username")
@XmlRootElement
public class Credentials implements Serializable { 
    @Id
    @SequenceGenerator(name = "Credentials_Gen", sequenceName = "CREDENTIALS_GEN", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "Credentials_Gen")
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @OneToOne
    @JoinColumn(name = "customer_id", nullable = false, unique = true)
    private Customer customer;

    @Transient
    private String jwtToken;

    public Credentials() {}
    
    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
     
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Customer getCustomer() {
        return customer;
    }

    public String getJwtToken() {
        return jwtToken;
    }

    public void setJwtToken(String jwtToken) {
        this.jwtToken = jwtToken;
    }

    public String generateJwtToken() {
        this.jwtToken = JWTUtil.generateToken(this.username);
        return this.jwtToken;
    }

    @Override
    public String toString() {
        return "Credentials{" + "id=" + id + ", username=" + username + '}';
    }
}
