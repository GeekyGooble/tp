package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DESC;
import static seedu.address.logic.parser.CliSyntax.PREFIX_END;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EVENT_ALIAS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EVENT_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_START;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_EVENTS;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.util.CollectionUtil;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.event.Event;
import seedu.address.model.event.EventAlias;
import seedu.address.model.event.EventName;

/**
 * Edits the details of an existing event in the address book.
 */
public class EditEventCommand extends Command {

    public static final String COMMAND_WORD = "edit-event";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Edits the event identified "
            + "by its event alias. "
            + "Existing values will be overwritten by the input values.\n"
            + "Parameters: "
            + PREFIX_EVENT_ALIAS + "ALIAS "
            + "[" + PREFIX_EVENT_NAME + "EVENT NAME] "
            + "[" + PREFIX_START + "START DATETIME] "
            + "[" + PREFIX_END + "END DATETIME] "
            + "[" + PREFIX_DESC + "DESCRIPTION]\n"
            + "Example: "
            + COMMAND_WORD + " "
            + PREFIX_EVENT_ALIAS + "TSC2025 "
            + PREFIX_START + "2025-09-19 18:30 "
            + PREFIX_DESC + "Venue changed to Indoor Stadium";

    public static final String MESSAGE_EDIT_EVENT_SUCCESS = "Edited Event: %1$s";
    public static final String MESSAGE_NOT_EDITED = "At least one field to edit must be provided.";
    public static final String MESSAGE_EVENT_NOT_FOUND = "Event not found.";
    public static final String MESSAGE_NO_EDITS_MADE = "There was no edits made to the event.";

    private final EventAlias eventAlias;
    private final EditEventDescriptor editEventDescriptor;

    /**
     * Constructor for an EditEventCommand
     * @param eventAlias of the event in the list to edit
     * @param editEventDescriptor details to edit the event with
     */
    public EditEventCommand(EventAlias eventAlias, EditEventDescriptor editEventDescriptor) {
        requireNonNull(eventAlias);
        requireNonNull(editEventDescriptor);

        this.eventAlias = eventAlias;
        this.editEventDescriptor = editEventDescriptor;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Event> eventList = model.getEventList();

        Event eventToEdit = eventList.stream()
                .filter(e -> e.getAlias().equalsIgnoreCase(eventAlias.toString()))
                .findFirst()
                .orElseThrow(() -> new CommandException(MESSAGE_EVENT_NOT_FOUND));

        Event editedEvent = createEditedEvent(eventToEdit, editEventDescriptor);

        if (eventToEdit.equals(editedEvent)) {
            throw new CommandException(MESSAGE_NO_EDITS_MADE);
        }

        assert eventToEdit.isSameEvent(editedEvent);

        model.setEvent(eventToEdit, editedEvent);
        model.updateFilteredEventList(PREDICATE_SHOW_ALL_EVENTS);
        return new CommandResult(String.format(MESSAGE_EDIT_EVENT_SUCCESS, Messages.format(editedEvent)));
    }

    /**
     * Creates and returns an {@code Event} with the details of {@code eventToEdit}
     * edited with {@code editEventDescriptor}.
     */
    private static Event createEditedEvent(Event eventToEdit, EditEventDescriptor editEventDescriptor)
            throws CommandException {
        assert eventToEdit != null;

        EventName updatedName = editEventDescriptor.getEventName().orElse(eventToEdit.getEventName());
        LocalDateTime updatedStart = editEventDescriptor.getStart().orElse(eventToEdit.getStart());
        LocalDateTime updatedEnd = editEventDescriptor.getEnd().orElse(eventToEdit.getEnd());
        String updatedDescription = editEventDescriptor.getDescription().orElse(eventToEdit.getDescription());

        try {
            return new Event(updatedName, eventToEdit.getEventAlias(), updatedStart, updatedEnd, updatedDescription);
        } catch (IllegalArgumentException e) {
            throw new CommandException(e.getMessage());
        }
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof EditEventCommand)) {
            return false;
        }

        EditEventCommand otherEditEventCommand = (EditEventCommand) other;
        return eventAlias.equals(otherEditEventCommand.eventAlias)
                && editEventDescriptor.equals(otherEditEventCommand.editEventDescriptor);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("eventAlias", eventAlias)
                .add("editEventDescriptor", editEventDescriptor)
                .toString();
    }

    /**
     * Stores the details to edit the event with. Each non-empty field value will replace the
     * corresponding field value of the event.
     */
    public static class EditEventDescriptor {
        private EventName name;
        private LocalDateTime start;
        private LocalDateTime end;
        private String description;

        /**
         * Returns true if at least one field is edited.
         */
        public boolean isAnyFieldEdited() {
            return CollectionUtil.isAnyNonNull(name, start, end, description);
        }

        public void setEventName(EventName name) {
            this.name = name;
        }

        public Optional<EventName> getEventName() {
            return Optional.ofNullable(name);
        }

        public void setStart(LocalDateTime start) {
            this.start = start;
        }

        public Optional<LocalDateTime> getStart() {
            return Optional.ofNullable(start);
        }

        public void setEnd(LocalDateTime end) {
            this.end = end;
        }

        public Optional<LocalDateTime> getEnd() {
            return Optional.ofNullable(end);
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Optional<String> getDescription() {
            return Optional.ofNullable(description);
        }

        @Override
        public boolean equals(Object other) {
            if (other == this) {
                return true;
            }

            // instanceof handles nulls
            if (!(other instanceof EditEventDescriptor)) {
                return false;
            }

            EditEventDescriptor otherEditEventDescriptor = (EditEventDescriptor) other;
            return Objects.equals(name, otherEditEventDescriptor.name)
                    && Objects.equals(start, otherEditEventDescriptor.start)
                    && Objects.equals(end, otherEditEventDescriptor.end)
                    && Objects.equals(description, otherEditEventDescriptor.description);
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .add("name", name)
                    .add("start", start)
                    .add("end", end)
                    .add("description", description)
                    .toString();
        }
    }
}
