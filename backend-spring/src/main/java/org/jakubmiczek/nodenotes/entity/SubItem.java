package org.jakubmiczek.nodenotes.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name="sub_items")
public class SubItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="sub_item_id")
    private Long subItemId;

    @Column(name="text", nullable = false)
    private String text;

    @Column(name="is_done", nullable = false)
    private boolean isDone;

    @ManyToOne
    @JoinColumn(name="parent_id")
    private SubItem parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("subItemId ASC")
    private List<SubItem> children;

    @ManyToOne
    @JoinColumn(name="task_id")
    private Task task;
}
