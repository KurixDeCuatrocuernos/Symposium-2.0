package com.sympos2.models;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * This is the class to creates Usuario objects, which represents student, titled and admin users in database by the parameter: "role".
 * @author KurixDeCuatroCuernos
 * @version 0.1.0
 * @see UserDetails
 */
@Document(collection="users")
public class Usuario implements UserDetails {
	@Id
	private String id;
	
	private String name;
	
	private LocalDate fechaNac;
	
	@Indexed(unique=true)
	private String email;
	
	private String password; // This parameter won't save in memory, only its serialization
	
	private String avatar;
	
	private String role;
	
// These are Student parameters
	
	private String studies;
	
	private String school;
	
// These are Administrator parameters	
	
	private Long phone;
	
// These are Titled parameters
	
	private String studies_title;
	
	private String study_place;
	
	private LocalDate title_date;
	
	private String title_img;

	public Usuario() { }
	
	/**
	 * Constructor for creating a "Student" user with the specified attributes.
	 * This constructor initializes an instance of the Usuario class with the 
	 * provided values for user details, including the user's ID, name, birth date, 
	 * email, password, avatar, studies, and school.
	 * 
	 * @param id String which collects the ObjectId of the user.
	 * @param name String with the user's name.
	 * @param fechaNac LocalDate with the user's birthday.
	 * @param email String with the user's email address.
	 * @param password String with the user's password.
	 * @param avatar String with the avatar image URL of the user.
	 * @param studies String with the studies the user is currently pursuing.
	 * @param school String with the institution where the user is studying.
	 */

	public Usuario(String id, String name, LocalDate fechaNac, String email, String password, String avatar, String role,
			String studies, String school) {
		this.id = id;
		this.name = name;
		this.fechaNac = fechaNac;
		this.email = email;
		this.password = password;
		this.avatar = avatar;
		this.studies = studies;
		this.school = school;
		this.role = role.toUpperCase();
	}
	
	/**
	 * Constructor for creating an "Administrator" user with the specified attributes.
	 * This constructor initializes an instance of the Usuario class with the 
	 * provided values for user details, including the user's ID, name, birth date, 
	 * email, password, avatar, and phone number. Additionally, the user's role 
	 * is set to "ADMIN".
	 * 
	 * @param id String which collects the ObjectId of the user.
	 * @param name String with the user's name.
	 * @param fechaNac LocalDate with the user's birthday.
	 * @param email String with the user's email address.
	 * @param password String with the user's password.
	 * @param avatar String with the avatar image URL of the user.
	 * @param phone Long with the user's phone number.
	 */
	public Usuario(String id, String name, LocalDate fechaNac, String email, String password, String avatar, String role,
			Long phone) {
		this.id = id;
		this.name = name;
		this.fechaNac = fechaNac;
		this.email = email;
		this.password = password;
		this.avatar = avatar;
		this.phone = phone;
		this.role = role.toUpperCase();
	}
	
	/**
	 * Constructor for creating a "Titled" user with the specified attributes.
	 * This constructor initializes an instance of the Usuario class with the 
	 * provided values for user details, including the user's ID, name, birth date, 
	 * email, password, avatar, study title, study place, title date, and title image.
	 * Additionally, the user's role is set to "TITLED".
	 * 
	 * @param id String which collects the ObjectId of the user.
	 * @param name String with the user's name.
	 * @param fechaNac LocalDate with the user's birthday.
	 * @param email String with the user's email address.
	 * @param password String with the user's password.
	 * @param avatar String with the avatar image URL of the user.
	 * @param studies_title String with the title of the user's studies.
	 * @param study_place String with the institution where the user completed her/his studies.
	 * @param title_date LocalDate with the date when the user obtained their title.
	 * @param title_img the image associated with the user's title.
	 */
	public Usuario(String id, String name, LocalDate fechaNac, String email, String password, String avatar, String role,
			String studies_title, String study_place, LocalDate title_date, String title_img) {
		this.id = id;
		this.name = name;
		this.fechaNac = fechaNac;
		this.email = email;
		this.password = password;
		this.avatar = avatar;
		this.studies_title = studies_title;
		this.study_place = study_place;
		this.title_date = title_date;
		this.title_img = title_img;
		this.role= role.toUpperCase();
	}
		

	// Getters and Setters
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public LocalDate getFechaNac() {
		return fechaNac;
	}
	public void setFechaNac(LocalDate fechaNac) {
		this.fechaNac = fechaNac;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getAvatar() {
		return avatar;
	}
	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public String getStudies() {
		return studies;
	}
	public void setStudies(String studies) {
		this.studies = studies;
	}
	public String getSchool() {
		return school;
	}
	public void setSchool(String school) {
		this.school = school;
	}
	public Long getPhone() {
		return phone;
	}
	public void setPhone(Long phone) {
		this.phone = phone;
	}
	public String getStudies_title() {
		return studies_title;
	}
	public void setStudies_title(String studies_title) {
		this.studies_title = studies_title;
	}
	public String getStudy_place() {
		return study_place;
	}
	public void setStudy_place(String study_place) {
		this.study_place = study_place;
	}
	public LocalDate getTitle_date() {
		return title_date;
	}
	public void setTitle_date(LocalDate title_date) {
		this.title_date = title_date;
	}
	public String getTitle_img() {
		return title_img;
	}
	public void setTitle_img(String title_img) {
		this.title_img = title_img;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(avatar, email, fechaNac, id, name, password, phone, role, school, studies, studies_title,
				study_place, title_date, title_img);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Usuario other = (Usuario) obj;
		return Objects.equals(avatar, other.avatar) && Objects.equals(email, other.email)
				&& Objects.equals(fechaNac, other.fechaNac) && Objects.equals(id, other.id)
				&& Objects.equals(name, other.name) && Objects.equals(password, other.password)
				&& Objects.equals(phone, other.phone) && Objects.equals(role, other.role)
				&& Objects.equals(school, other.school) && Objects.equals(studies, other.studies)
				&& Objects.equals(studies_title, other.studies_title) && Objects.equals(study_place, other.study_place)
				&& Objects.equals(title_date, other.title_date) && Objects.equals(title_img, other.title_img);
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Usuario [id=").append(id).append(", name=").append(name).append(", fechaNac=").append(fechaNac)
				.append(", email=").append(email).append(", password=").append(password).append(", avatar=")
				.append(avatar).append(", role=").append(role).append(", studies=").append(studies).append(", school=")
				.append(school).append(", phone=").append(phone).append(", studies_title=").append(studies_title)
				.append(", study_place=").append(study_place).append(", title_date=").append(title_date)
				.append(", title_img=").append(title_img).append("]");
		return builder.toString();
	}
	
	// METHODS FOR SPRING SECURITY 
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
	}

    @Override
    public String getUsername() {
        return email; // We use the email as username
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
	
	
	
}
