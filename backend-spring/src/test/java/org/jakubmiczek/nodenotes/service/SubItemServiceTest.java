package org.jakubmiczek.nodenotes.service;

import org.jakubmiczek.nodenotes.controller.dto.SubItemRequest;
import org.jakubmiczek.nodenotes.entity.*;
import org.jakubmiczek.nodenotes.exception.SubItemDoesNotExistException;
import org.jakubmiczek.nodenotes.exception.TaskAccessDeniedException;
import org.jakubmiczek.nodenotes.exception.TaskDoesNotExistException;
import org.jakubmiczek.nodenotes.repository.SubItemRepository;
import org.jakubmiczek.nodenotes.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SubItemServiceTest {
    @InjectMocks
    SubItemService subItemService;

    @Mock
    SubItemRepository subItemRepository;

    @Mock
    TaskRepository taskRepository;

    private User createUser() {
        User user = new User();
        user.setUserId(1L);
        user.setUsername("username");
        user.setPassword("password");

        return user;
    }

    private Task createTask(User user) {
        Task task = new Task();
        task.setTaskId(1L);
        task.setTitle("task");
        task.setDescription("");
        task.setStatus(TaskStatus.TODO);
        task.setType(TaskType.TREE);
        task.setUser(user);

        return task;
    }

    private SubItem createSubItemForUser() {
        User user = createUser();
        Task task = createTask(user);

        SubItem subItem = new SubItem();
        subItem.setSubItemId(1L);
        subItem.setText("text");
        subItem.setDone(false);
        subItem.setParent(null);
        subItem.setChildren(List.of());
        subItem.setTask(task);
        return subItem;
    }

    @Test
    void shouldAddParentSubItemCorrectly() {
        User user = createUser();
        Task task = createTask(user);
        SubItemRequest request = new SubItemRequest("text");

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        subItemService.addSubItem(request, null, 1L, "username");

        ArgumentCaptor<SubItem> captor = ArgumentCaptor.forClass(SubItem.class);
        verify(subItemRepository).save(captor.capture());
        SubItem subItem = captor.getValue();

        assertThat(subItem.getText()).isEqualTo("text");
        assertThat(subItem.getParent()).isNull();
        assertThat(subItem.getTask()).isEqualTo(task);
    }

    @Test
    void shouldAddChildSubItemCorrectly() {
        SubItem parentItem = createSubItemForUser();
        SubItemRequest request = new SubItemRequest("child text");

        when(subItemRepository.findById(1L)).thenReturn(Optional.of(parentItem));
        subItemService.addSubItem(request, 1L, 1L, "username");

        ArgumentCaptor<SubItem> captor = ArgumentCaptor.forClass(SubItem.class);
        verify(subItemRepository).save(captor.capture());
        SubItem subItem = captor.getValue();

        assertThat(subItem.getText()).isEqualTo("child text");
        assertThat(subItem.getParent()).isEqualTo(parentItem);
        assertThat(subItem.getTask()).isEqualTo(parentItem.getTask());
    }

    @Test
    void shouldThrowSubItemDoesNotExistExceptionWhenCreatingSubItem() {
        when(subItemRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> subItemService.addSubItem(new SubItemRequest("text"), 1L, null, "username"))
                .isInstanceOf(SubItemDoesNotExistException.class);
    }

    @Test
    void shouldThrowTaskDoesNotExistExceptionWhenCreatingSubItem() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> subItemService.addSubItem(new SubItemRequest("text"), null, 1L, "username"))
                .isInstanceOf(TaskDoesNotExistException.class);
    }

    @Test
    void shouldThrowTaskAccessDeniedExceptionWhenCreatingSubItem() {
        SubItem subItem = createSubItemForUser();
        when(subItemRepository.findById(1L)).thenReturn(Optional.of(subItem));

        assertThatThrownBy(() -> subItemService.addSubItem(new SubItemRequest("text"), 1L, null, "user"))
                .isInstanceOf(TaskAccessDeniedException.class);
    }

    @Test
    void shouldUpdateSubItemTextCorrectly() {
        SubItem subItem = createSubItemForUser();

        when(subItemRepository.findById(1L)).thenReturn(Optional.of(subItem));
        subItemService.updateSubItemText("new text", 1L, "username");

        assertThat(subItem.getText()).isEqualTo("new text");
    }

    @Test
    void shouldThrowSubItemDoesNotExistExceptionWhenUpdatingSubItemsText() {
        when(subItemRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> subItemService.updateSubItemText("new text", 1L, "username"))
                .isInstanceOf(SubItemDoesNotExistException.class);
    }

    @Test
    void shouldThrowTaskAccessDeniedExceptionWhenUpdatingSubItemsText() {
        SubItem subItem = createSubItemForUser();
        when(subItemRepository.findById(1L)).thenReturn(Optional.of(subItem));

        assertThatThrownBy(() -> subItemService.updateSubItemText("new text", 1L, "user"))
                .isInstanceOf(TaskAccessDeniedException.class);
    }

    @Test
    void shouldUpdateSubItemStatusWithMarkingChildrenAndParentsAsDoneCorrectly() {
        User user = createUser();
        Task task = createTask(user);

        SubItem parentItem = new SubItem();
        parentItem.setDone(false);
        parentItem.setTask(task);

        SubItem childItem1 = new SubItem();
        childItem1.setDone(false);
        childItem1.setParent(parentItem);
        childItem1.setTask(task);

        SubItem childItem2 = new SubItem();
        childItem2.setDone(true);
        childItem2.setParent(parentItem);
        childItem2.setChildren(List.of());
        childItem2.setTask(task);

        SubItem grandChild = new SubItem();
        grandChild.setDone(false);
        grandChild.setParent(childItem1);
        grandChild.setChildren(List.of());
        grandChild.setTask(task);

        childItem1.setChildren(List.of(grandChild));
        parentItem.setChildren(List.of(childItem1, childItem2));
        task.setItems(List.of(parentItem, childItem1, childItem2, grandChild));

        when(subItemRepository.findById(1L)).thenReturn(Optional.of(childItem1));
        subItemService.updateSubItemStatus(false, 1L, "username");

        assertThat(childItem1.isDone()).isTrue();
        assertThat(grandChild.isDone()).isTrue();
        assertThat(parentItem.isDone()).isTrue();
        assertThat(task.getStatus()).isEqualTo(TaskStatus.DONE);
    }

    @Test
    void shouldUpdateSubItemStatusWithUnmarkingParentsCorrectly() {
        User user = createUser();
        Task task = createTask(user);

        SubItem parentItem = new SubItem();
        parentItem.setDone(true);
        parentItem.setTask(task);

        SubItem childItem1 = new SubItem();
        childItem1.setDone(true);
        childItem1.setParent(parentItem);
        childItem1.setTask(task);

        SubItem childItem2 = new SubItem();
        childItem2.setDone(true);
        childItem2.setParent(parentItem);
        childItem2.setTask(task);

        parentItem.setChildren(List.of(childItem1, childItem2));
        task.setItems(List.of(parentItem, childItem1, childItem2));

        when(subItemRepository.findById(1L)).thenReturn(Optional.of(childItem1));
        subItemService.updateSubItemStatus(true, 1L, "username");

        assertThat(childItem1.isDone()).isFalse();
        assertThat(parentItem.isDone()).isFalse();
        assertThat(task.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
    }

    @Test
    void shouldNotMarkParentAsDoneWhenSiblingIsNotDone() {
        User user = createUser();
        Task task = createTask(user);

        SubItem parentItem = new SubItem();
        parentItem.setDone(false);
        parentItem.setTask(task);

        SubItem childItem1 = new SubItem();
        childItem1.setDone(false);
        childItem1.setParent(parentItem);
        childItem1.setChildren(List.of());
        childItem1.setTask(task);

        SubItem childItem2 = new SubItem();
        childItem2.setDone(false);
        childItem2.setParent(parentItem);
        childItem2.setChildren(List.of());
        childItem2.setTask(task);

        parentItem.setChildren(List.of(childItem1, childItem2));
        task.setItems(List.of(parentItem, childItem1, childItem2));

        when(subItemRepository.findById(1L)).thenReturn(Optional.of(childItem1));
        subItemService.updateSubItemStatus(false, 1L, "username");

        assertThat(childItem1.isDone()).isTrue();
        assertThat(parentItem.isDone()).isFalse();
    }

    @Test
    void shouldThrowSubItemDoesNotExistExceptionWhenUpdatingSubItemsStatus() {
        when(subItemRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> subItemService.updateSubItemStatus(true, 1L, "username"))
                .isInstanceOf(SubItemDoesNotExistException.class);
    }

    @Test
    void shouldThrowTaskAccessDeniedExceptionWhenUpdatingSubItemsStatus() {
        SubItem subItem = createSubItemForUser();
        when(subItemRepository.findById(1L)).thenReturn(Optional.of(subItem));

        assertThatThrownBy(() -> subItemService.updateSubItemStatus(true, 1L, "user"))
                .isInstanceOf(TaskAccessDeniedException.class);
    }

    @Test
    void shouldDeleteSubItemCorrectly() {
        SubItem subItem = createSubItemForUser();
        when(subItemRepository.findById(1L)).thenReturn(Optional.of(subItem));

        subItemService.deleteSubItem(1L, "username");

        verify(subItemRepository).delete(subItem);
    }

    @Test
    void shouldThrowSubItemDoesNotExistExceptionWhenDeletingSubItem() {
        when(subItemRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> subItemService.deleteSubItem(1L, "username"))
                .isInstanceOf(SubItemDoesNotExistException.class);
    }

    @Test
    void shouldThrowTaskAccessDeniedExceptionWhenDeletingSubItem() {
        SubItem subItem = createSubItemForUser();
        when(subItemRepository.findById(1L)).thenReturn(Optional.of(subItem));

        assertThatThrownBy(() -> subItemService.deleteSubItem(1L, "user"))
                .isInstanceOf(TaskAccessDeniedException.class);
    }
}
