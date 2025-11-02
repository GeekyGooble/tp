package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TODO_DESCRIPTION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TODO_NAME;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_TODOS;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.CollectionUtil;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.todo.Todo;
import seedu.address.model.todo.TodoName;

/**
 * Edits the details of an existing Todo, identified by its index in the displayed todo list.
 */
public class EditTodoCommand extends Command {

    public static final String COMMAND_WORD = "edit-todo";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Edits the todo identified "
            + "by its index number used in the displayed todo list. "
            + "Existing values will be overwritten by the input values.\n"
            + "Parameters: INDEX (must be a positive integer) "
            + "[" + PREFIX_TODO_NAME + "TODO_NAME] "
            + "[" + PREFIX_TODO_DESCRIPTION + "DESCRIPTION] "
            + "[" + PREFIX_NAME + "PERSON_NAME]\n"
            + "Example: " + COMMAND_WORD + " 1 " + PREFIX_TODO_DESCRIPTION + "Meet John regarding events plan";

    public static final String MESSAGE_EDIT_TODO_SUCCESS = "Edited Todo: %1$s";
    public static final String MESSAGE_NOT_EDITED = "At least one field to edit must be provided.";
    public static final String MESSAGE_DUPLICATE_TODO = "This todo already exists in the address book";
    public static final String MESSAGE_NO_EDITS_MADE = "There was no edits made to the todo. ";

    private final Index targetIndex;
    private final EditTodoDescriptor editTodoDescriptor;

    /**
     * Constructor for an EditTodoCommand
     * @param targetIndex based on the indexing listed in the todo list
     * @param editTodoDescriptor details to edit the todo with
     */
    public EditTodoCommand(Index targetIndex, EditTodoDescriptor editTodoDescriptor) {
        requireNonNull(targetIndex);
        requireNonNull(editTodoDescriptor);

        this.targetIndex = targetIndex;
        this.editTodoDescriptor = editTodoDescriptor;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Todo> lastShownList = model.getFilteredTodoList();

        if (targetIndex.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_TODO_DISPLAYED_INDEX);
        }

        Todo todoToEdit = lastShownList.get(targetIndex.getZeroBased());

        if (!editTodoDescriptor.isAnyFieldEdited()) {
            throw new CommandException(MESSAGE_NOT_EDITED);
        }

        Todo editedTodo = createEditedTodo(todoToEdit, editTodoDescriptor);

        if (todoToEdit.equals(editedTodo)) {
            throw new CommandException(MESSAGE_NO_EDITS_MADE);
        }

        if (!todoToEdit.isSameTodo(editedTodo) && model.hasTodo(editedTodo)) {
            throw new CommandException(MESSAGE_DUPLICATE_TODO);
        }

        Name updatedContact = editedTodo.getContactName();
        if (updatedContact != null && !hasContactName(model, updatedContact)) {
            throw new CommandException("Contact not found: " + updatedContact.fullName);
        }

        model.setTodo(todoToEdit, editedTodo);
        model.updateFilteredTodoList(PREDICATE_SHOW_ALL_TODOS);

        return new CommandResult(String.format(MESSAGE_EDIT_TODO_SUCCESS, Messages.format(editedTodo)));
    }

    /**
     * Checks if the input contact name exists in the current PersonList
     */
    private boolean hasContactName(Model model, Name name) {
        return model.getAddressBook().getPersonList().stream()
                .map(Person::getName)
                .anyMatch(name::equals);
    }

    /**
     * Creates and returns {@code Todo} with the details of {@code todoToEdit}
     * edited with {@code editTodoDescriptor}
     */
    private static Todo createEditedTodo(Todo todoToEdit, EditTodoDescriptor editTodoDescriptor) {
        assert todoToEdit != null;

        TodoName updatedName = editTodoDescriptor.getTodoName().orElse(todoToEdit.getTodoName());
        String updatedDescription = editTodoDescriptor.getDescription().orElse(todoToEdit.getTodoDescription());
        Name updatedContactName;
        if (editTodoDescriptor.isContactUnlinked) {
            updatedContactName = null;
        } else {
            updatedContactName = editTodoDescriptor.getContactName().orElse(todoToEdit.getContactName());
        }
        boolean updatedIsCompleted = editTodoDescriptor.getIsCompleted().orElse(todoToEdit.getIsCompleted());

        return new Todo(updatedName, updatedDescription, updatedContactName, updatedIsCompleted);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof EditTodoCommand)) {
            return false;
        }

        EditTodoCommand otherEditTodoCommand = (EditTodoCommand) other;
        return targetIndex.equals(otherEditTodoCommand.targetIndex)
                && editTodoDescriptor.equals(otherEditTodoCommand.editTodoDescriptor);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .add("editTodoDescriptor", editTodoDescriptor)
                .toString();
    }

    /**
     * Stores the details to edit the todo with. Each non-empty field value wil replace the
     * corresponding field of the todo task.
     */
    public static class EditTodoDescriptor {
        private TodoName todoName;
        private String todoDescription;
        private Name contactName;
        private Boolean isCompleted;

        private boolean isContactUnlinked = false;

        /**
         * Returns true if at least one field is edited
         */
        public boolean isAnyFieldEdited() {
            return CollectionUtil.isAnyNonNull(todoName, todoDescription, contactName, isCompleted)
                    || isContactUnlinked;
        }

        public void setTodoName(TodoName todoName) {
            this.todoName = todoName;
        }

        public Optional<TodoName> getTodoName() {
            return Optional.ofNullable(todoName);
        }

        public void setTodoDescription(String description) {
            this.todoDescription = description;
        }

        public Optional<String> getDescription() {
            return Optional.ofNullable(todoDescription);
        }

        public void setContactName(Name contactName) {
            this.contactName = contactName;
        }

        public Optional<Name> getContactName() {
            return Optional.ofNullable(contactName);
        }

        public void setCompleted(Boolean isCompleted) {
            this.isCompleted = isCompleted;
        }

        public Optional<Boolean> getIsCompleted() {
            return Optional.ofNullable(isCompleted);
        }

        public void markContactUnlinked() {
            this.isContactUnlinked = true;
        }

        @Override
        public boolean equals(Object other) {
            if (other == this) {
                return true;
            }

            if (!(other instanceof EditTodoDescriptor)) {
                return false;
            }

            EditTodoDescriptor otherDescriptor = (EditTodoDescriptor) other;
            return Objects.equals(todoName, otherDescriptor.todoName)
                    && Objects.equals(todoDescription, otherDescriptor.todoDescription)
                    && Objects.equals(contactName, otherDescriptor.contactName)
                    && Objects.equals(isCompleted, otherDescriptor.isCompleted)
                    && Objects.equals(isContactUnlinked, otherDescriptor.isContactUnlinked);
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .add("todoName", todoName)
                    .add("todoDescription", todoDescription)
                    .add("contactName", contactName)
                    .add("isCompleted", isCompleted)
                    .toString();
        }

    }


}
