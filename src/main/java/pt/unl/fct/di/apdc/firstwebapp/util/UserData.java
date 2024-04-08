package pt.unl.fct.di.apdc.firstwebapp.util;

public class UserData {
	public String username;
	public String password;
	public String password2; // Campo adicional para confirmar a password
	public String gmail;
	public Integer phone; // Agora é um campo obrigatório
	// Campos opcionais
	public String perfil;
	public String ocupacao;
	public String localTrabalho;
	public String morada;
	public String cp;
	public String nome;
	public Integer nif;
	
	 public UserData() {

	    }


	public UserData(String username, String password, String gmail, int phone, int nif, String password2,
			String perfil, String ocupacao, String localTrabalho, String morada, String cp, String nome) {
		this.username = username;
		this.password = password;
		this.password2 = password2;
		this.gmail = gmail;
		this.phone = phone;
		this.perfil = perfil;
		this.ocupacao = ocupacao;
		this.localTrabalho = localTrabalho;
		this.morada = morada;
		this.cp = cp;
		this.nif = nif;
		this.nome = nome;

	}
	
	// Métodos getters
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getPassword2() {
        return password2;
    }

    public String getGmail() {
        return gmail;
    }

    public Integer getPhone() {
        return phone;
    }

    public String getPerfil() {
        return perfil;
    }

    public String getOcupacao() {
        return ocupacao;
    }

    public String getLocalTrabalho() {
        return localTrabalho;
    }

    public String getMorada() {
        return morada;
    }

    public String getCp() {
        return cp;
    }

    public Integer getNif() {
        return nif;
    }
}
