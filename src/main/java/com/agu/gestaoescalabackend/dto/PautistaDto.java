package com.agu.gestaoescalabackend.dto;

import com.agu.gestaoescalabackend.entities.Pautista;
import com.agu.gestaoescalabackend.enums.GrupoPautista;
import com.agu.gestaoescalabackend.enums.StatusPautista;
import com.agu.gestaoescalabackend.util.Conversor;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;


@Getter
@Setter
@ToString
@NoArgsConstructor
public class PautistaDto implements Serializable, Comparable<PautistaDto> {
    private static final long serialVersionUID = 1L;

    // ATRIBUTOS DE IDENTIFICAÇÃO
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;
    @NotBlank
    private String nome;
    @NotNull
    @JsonProperty(value = "grupo")
    private GrupoPautista grupoPautista;

    // ATRIBUTOS DE ESTADO
    @NotNull
    @JsonProperty(value = "status")
    private StatusPautista statusPautista;
    private LocalDate dataInicial;
    private LocalDate dataFinal;

    // ATRIBUTOS DE ESCALA
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer saldo;
    @NotNull
    private Integer peso;
    private Integer saldoPeso;

    // ATRIBUTOS DE RELACIONAMENTO
    @JsonBackReference
    private List<PautaDto> pautas;

    /*------------------------------------------------
     METODOS DE CONVERSÃO
    ------------------------------------------------*/

    public Pautista toEntity(){
        return Conversor.converter(this, Pautista.class);
    }

    public boolean estaDisponivel(LocalDate dataPassada){
        for (PautaDto pauta : this.getPautas())
            if(pauta.getData().equals(dataPassada))
                return false;
        return true;
    }

    public void atualizarSaldo(int valor, PautaDto pauta){
        this.setSaldo(this.getSaldo() + valor);
        this.setSaldoPeso(this.getSaldo() * this.getPeso());
        this.pautas.add(pauta);
    }

    @Override
    public int compareTo(PautistaDto outroPautista) {
        if (this.saldoPeso < outroPautista.saldoPeso) {
            return -1;
        } else if (this.saldoPeso > outroPautista.saldoPeso) {
            return 1;
        }
        return 0;
    }

}
