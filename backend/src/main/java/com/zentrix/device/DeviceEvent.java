package com.zentrix.device;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "EVENTO_DISPOSITIVO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EventoDispositivoId")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "DispositivoId")
    private Device device;

    @Column(name = "Tipo", nullable = false, length = 30)
    private String type;

    @Column(name = "ValorJson", columnDefinition = "NVARCHAR(MAX)")
    private String valueJson;

    @Column(name = "FechaEvento", nullable = false, updatable = false)
    private LocalDateTime eventDate;

    @PrePersist
    void onCreate() {
        if (eventDate == null) {
            eventDate = LocalDateTime.now();
        }
    }
}
