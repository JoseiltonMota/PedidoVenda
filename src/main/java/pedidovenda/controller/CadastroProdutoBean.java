package pedidovenda.controller;

import javax.inject.Named;
import javax.enterprise.context.RequestScoped;

@Named
@RequestScoped
public class CadastroProdutoBean {
	
	public void salvar(){
		throw new RuntimeException("Teste de exceção");
	}
	
}
