package com.estudo.minhasfinancas.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.estudo.minhasfinancas.exception.ErroAutenticacao;
import com.estudo.minhasfinancas.exception.RegraNegocioException;
import com.estudo.minhasfinancas.model.entity.Usuario;
import com.estudo.minhasfinancas.model.repository.UsuarioRepository;
import com.estudo.minhasfinancas.service.impl.UsuarioServiceImpl;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class UsuarioServiceTest {	
	
	@MockBean	
	UsuarioRepository repository;
	
	@SpyBean
	UsuarioServiceImpl service;
	
	@Test
	public void deveSalvarUmUsuario() {
		Mockito.doNothing().when(service).validarEmail(Mockito.anyString());
		
		Usuario usuario = Usuario.builder()
							.id(1l)
							.nome("nome")
							.email("email@email.com")
							.senha("senha").build();
		
		Mockito.when(repository.save(Mockito.any(Usuario.class))).thenReturn(usuario);
		
		Usuario usuarioSalvo = service.salvarUsuario(new Usuario());
		
		assertNotNull(usuarioSalvo);
		assertEquals(usuarioSalvo.getId(), 1l);
		assertEquals(usuarioSalvo.getNome(), "nome");
		assertEquals(usuarioSalvo.getEmail(), "email@email.com");
		assertEquals(usuarioSalvo.getSenha(), "senha");
	}
	
	@Test
	public void naoDeveSalvarUmUsuarioComEmailJaCadastrado() {
		String email= "email@email.com";
		Usuario usuario = Usuario.builder().email(email).build();
		Mockito.doThrow(RegraNegocioException.class).when(service).validarEmail(email);
		
		Exception exception = assertThrows(RegraNegocioException.class,() -> {
			service.salvarUsuario(usuario);
			});
		
		Mockito.verify(repository, Mockito.never()).save(usuario);
		
		assertNotNull(exception);
	}
	
	@Test
	public void deveAutenticarUmUsuarioComSucesso() {
		String email = "email@email.com";
		String senha = "senha";
		
		
		Usuario usuario = Usuario.builder().email(email).senha(senha).id(1l).build();
		Mockito.when(repository.findByEmail(email)).thenReturn(Optional.of(usuario));
		
		Usuario result = service.autenticar(email, senha);
		
		
		Assertions.assertNotNull(result);
	}	
	
	
	@Test
	public void deveLancarErroQuandoNaoEncontrarUsuarioCadastradoComOEmailInformado() {
		Mockito.when(repository.findByEmail(Mockito.anyString())).thenReturn(Optional.empty());
		
		Exception exception = assertThrows(ErroAutenticacao.class, () -> {
			service.autenticar("email@email.com", "senha");
		});
		
		String expectedMessage = "Usuário não encontrado para o email informado.";
		String actualMessage = exception.getMessage();
		
		assertEquals(expectedMessage, actualMessage);		
		
	}
	
	@Test
	public void deveLancarErroQuandoSenhaNaoBater() {
		String senha = "senha";
		
		Usuario usuario = Usuario.builder().email("email@email.com").senha(senha).build();
		Mockito.when(repository.findByEmail(Mockito.anyString())).thenReturn(Optional.of(usuario));
		
		
		Exception exception = assertThrows(ErroAutenticacao.class, () ->{
			service.autenticar("email@email.com", "123");
		}) ;
		
		String expectedMessage = "Senha inválida.";
		String actualMessage = exception.getMessage();
		
		assertEquals(expectedMessage, actualMessage);
	}	

	@Test
	public void deveValidarEmail() {	
		Mockito.when(repository.existsByEmail(Mockito.anyString())).thenReturn(true);
		
		assertThrows(RegraNegocioException.class, () -> {
				service.validarEmail("email@email.com");
			}
		);
	}
	
	@Test
	public void deveLancarErroAoValidarEmailQuandoExistirEmailCadastrado() {
		Mockito.when(repository.existsByEmail(Mockito.anyString())).thenReturn(true);
		
		Exception exception = assertThrows(RegraNegocioException.class, () -> { 
				service.validarEmail("email@email.com");
			}
		);
		
		String expectedMessage = "Já existe um usuário cadastrado com esse email";
		String actualMessage = exception.getMessage();
		
		assertTrue(actualMessage.contains(expectedMessage));
	}

}
