package com.estudo.minhasfinancas.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.estudo.minhasfinancas.model.entity.Lancamento;

public interface LancamentoRepository extends JpaRepository<Lancamento, Long>{

}
