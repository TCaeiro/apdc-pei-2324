package pt.unl.fct.di.apdc.firstwebapp.util;

public class ChangeAtrData {
	public String username;
	public String password;
	public String state;
	public String role;
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

	public ChangeAtrData() {

	}

	public ChangeAtrData(String username, String password, String gmail, int phone, int nif, String state,
			String perfil, String ocupacao, String localTrabalho, String morada, String cp, String nome, String role) {
		this.username = username;
		this.password = password;
		this.state = state;
		this.gmail = gmail;
		this.phone = phone;
		this.perfil = perfil;
		this.ocupacao = ocupacao;
		this.localTrabalho = localTrabalho;
		this.morada = morada;
		this.cp = cp;
		this.nif = nif;
		this.nome = nome;
		this.role = role;
	}
}