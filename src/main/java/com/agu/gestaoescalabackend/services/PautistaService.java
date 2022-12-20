package com.agu.gestaoescalabackend.services;

import com.agu.gestaoescalabackend.dto.PautaDto;
import com.agu.gestaoescalabackend.dto.PautaOnlyDto;
import com.agu.gestaoescalabackend.dto.PautistaDto;
import com.agu.gestaoescalabackend.entities.Pauta;
import com.agu.gestaoescalabackend.entities.Pautista;
import com.agu.gestaoescalabackend.enums.GrupoPautista;
import com.agu.gestaoescalabackend.enums.StatusPautista;
import com.agu.gestaoescalabackend.repositories.PautistaRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PautistaService {

    private PautistaRepository pautistaRepository;

    private PautaService pautaService;

    public boolean estaDisponivel(PautistaDto pautista, LocalDate dataPassada){
        List<PautaOnlyDto> pautas = pautaService.findByPautistaAndMes(pautista.getId(),dataPassada);
        pautas.addAll(pautista.getPautas());
        for (PautaOnlyDto pauta : pautista.getPautas())
            if(pauta.getData().equals(dataPassada))
                return false;
        return true;
    }
    
    
    @Transactional(readOnly = true)
    public List<Pautista> findAll() {
        return pautistaRepository.findAllByOrderBySaldoDesc();
    }

    @Transactional(readOnly = true)
    public List<PautistaDto> findByStatus(List<StatusPautista> status) {
        return pautistaRepository.findAllByStatusPautistaInOrderByNomeAsc(status)
                .stream()
                .map(Pautista::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
	public List<PautistaDto> findAllByGrupoPautistaAndStatusPautistaOrderBySaldoPesoAsc(GrupoPautista grupoPautista, StatusPautista statusPautista){
		
		return pautistaRepository.findAllByGrupoPautistaAndStatusPautistaOrderBySaldoPesoAsc(grupoPautista, StatusPautista.ATIVO)
            .stream()
            .map(Pautista::toDto)
            .collect(Collectors.toList());
	}

    @Transactional
    public List<PautistaDto> findAllByStatusPautistaOrderBySaldoPesoAsc (StatusPautista statusPautista){
		
		return pautistaRepository.findAllByStatusPautistaOrderBySaldoPesoAsc(StatusPautista.ATIVO)
            .stream()
            .map(Pautista::toDto)
            .collect(Collectors.toList());
	}

    @Transactional(readOnly = true)
    public List<Pautista> findAllAvailablePautistas(LocalDate data) {

        List<StatusPautista> statusPautistas = new ArrayList<>();
        statusPautistas.add(StatusPautista.ATIVO);
        statusPautistas.add(StatusPautista.INATIVO);

        // Busca todos os pautistas ativos no banco
        List<Pautista> pautistaList = pautistaRepository.findAllByStatusPautistaInOrderByNomeAsc(statusPautistas);
        List<Pautista> pautistaRetorno = new ArrayList<>();

        for (Pautista pautista : pautistaList){

            if (estaDisponivel(pautista.toDto(),data))
                pautistaRetorno.add(pautista);
        }

        return pautistaRetorno;
    }

    @Transactional
    public PautistaDto save(PautistaDto pautistaDto) {

        Pautista pautista = pautistaDto.toEntity().forSave();
        definirSaldo(pautista);
        return pautistaRepository.save(pautista).toDto();
    }

    @Transactional
    public PautistaDto update(Long id, PautistaDto pautistaDto) {


        Optional<Pautista> pautistaOptional = pautistaRepository.findById(id);
        if (pautistaOptional.isEmpty())
            return null;
        Pautista pautista = pautistaOptional.get().forUpdate(pautistaDto);
        return pautistaRepository.save(pautista).toDto();

    }

    @Transactional
    public void delete(Long procuradorId) {
        if (pautistaRepository.existsById(procuradorId))
            pautistaRepository.deleteById(procuradorId);
    }

    /*------------------------------------------------
    METODOS DE NEGÓCIO
    ------------------------------------------------*/

    private void definirSaldo(Pautista pautista) {
        int media = 0;
        List<Pautista> pautistas = pautistaRepository.findAll();
        for (Pautista pautistaFor : pautistas) {
            media += pautistaFor.getSaldo();
        }
        pautista.setSaldo(media);
    }

}
