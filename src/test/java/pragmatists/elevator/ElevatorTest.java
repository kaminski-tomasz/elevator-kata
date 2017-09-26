package pragmatists.elevator;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import pragmatists.elevator.door.Door;
import pragmatists.elevator.door.Door.DoorState;
import pragmatists.elevator.engine.Engine;
import pragmatists.elevator.engine.Engine.Direction;

import static org.mockito.Mockito.*;

@RunWith(JUnitParamsRunner.class)
public class ElevatorTest {

    private final Door door = mock(Door.class);
    private final Engine engine = mock(Engine.class);

    private ElevatorBuilder anElevator() {
        return ElevatorBuilder.anElevator(door, engine);
    }

    @Test
    public void should_register_itself_to_collaborators() {

        Elevator elevator = anElevator().build();

        verify(door).setListener(elevator);
        verify(engine).setListener(elevator);
    }

    @Test
    public void should_reset_engine_at_start() {

        Elevator elevator = anElevator().startingAt(3).build();

        elevator.run();

        verify(engine).reset(Floor.ofLevel(3));
    }

    @Test
    public void should_open_door_at_start() {

        Elevator elevator = anElevator().build();

        elevator.run();

        verify(door).open();
    }

    // elevator_goes_to_floor_1st

    @Test
    @Parameters({"-2", "-1", "1", "2"})
    public void should_close_door_after_request(int requested) {

        Elevator elevator = anElevator().build();

        elevator.floorRequested(Floor.ofLevel(requested));

        verify(door).close();
    }

    @Test
    @Parameters({"1", "2", "3", "4"})
    public void should_start_engine_up_after_closing_door(int requested) {

        Elevator elevator = anElevator().build();

        elevator.floorRequested(Floor.ofLevel(requested));
        elevator.doorStateChanged(DoorState.CLOSED);

        verify(engine).start(Direction.UP);
    }

    @Test
    @Parameters({"1", "-1"})
    public void should_stop_engine_at_requested_floor(int requested) {

        Elevator elevator = anElevator().build();

        elevator.floorRequested(Floor.ofLevel(requested));
        elevator.doorStateChanged(DoorState.CLOSED);
        elevator.floorReached(Floor.ofLevel(requested));

        verify(engine).stop();
    }

    @Test
    @Parameters({"1", "-1"})
    public void should_open_door_when_engine_stopped_at_requested_floor(int requested) {

        Elevator elevator = anElevator().build();

        elevator.floorRequested(Floor.ofLevel(requested));
        elevator.doorStateChanged(DoorState.CLOSED);
        elevator.floorReached(Floor.ofLevel(requested));
        elevator.engineStopped();

        verify(door).open();
    }

    // elevator_goes_to_floor_minus_1st

    @Test
    @Parameters({"-1", "-2", "-3", "-4"})
    public void should_start_engine_down_after_closing_door(int requestedLevel) {

        Elevator elevator = anElevator().build();

        elevator.floorRequested(Floor.ofLevel(requestedLevel));
        elevator.doorStateChanged(DoorState.CLOSED);

        verify(engine).start(Direction.DOWN);
    }

    // elevator_goes_to_floor_2nd

    @Test
    public void should_avoid_not_requested_floors_when_moving_up() {

        Elevator elevator = anElevator().build();

        elevator.floorRequested(Floor.ofLevel(2));
        elevator.doorStateChanged(DoorState.CLOSED);
        elevator.floorReached(Floor.ofLevel(1));

        verify(engine, times(0)).stop();
        verify(door, times(0)).open();
    }

    // elevator_goes_to_floor_minus_2nd

    @Test
    public void should_avoid_not_requested_floors_when_moving_down() {

        Elevator elevator = anElevator().build();

        elevator.floorRequested(Floor.ofLevel(-2));
        elevator.doorStateChanged(DoorState.CLOSED);
        elevator.floorReached(Floor.ofLevel(-1));

        verify(engine, times(0)).stop();
        verify(door, times(0)).open();
    }

    // elevator_goes_to_floor_2nd_and_then_to_floor_4th

    @Test
    public void should_close_door_when_new_request_arrive() {

        Elevator elevator = anElevator().build();

        elevator.floorRequested(Floor.ofLevel(1));
        elevator.doorStateChanged(DoorState.CLOSED);
        elevator.floorReached(Floor.ofLevel(1));
        elevator.engineStopped();
        elevator.doorStateChanged(DoorState.OPENED);
        elevator.floorRequested(Floor.ofLevel(2));

        verify(door, times(2)).close();
    }

    // elevator_goes_to_floor_minus_2nd_and_then_to_floor_minus_4th

    // elevator_goes_to_floor_2nd_and_then_to_floor_4th_ignoring_request_order

    // elevator_goes_to_floor_2nd_and_then_to_floor_4th_ignoring_request_order

    @Test
    public void should_not_close_door_twice_when_multiple_floors_requested() {

        Elevator elevator = anElevator().build();

        elevator.floorRequested(Floor.ofLevel(4));
        elevator.floorRequested(Floor.ofLevel(2));

        verify(door, times(1)).close();
    }

    @Test
    public void should_start_engine_up_when_multiple_floors_requested() {

        Elevator elevator = anElevator().build();

        elevator.floorRequested(Floor.ofLevel(4));
        elevator.floorRequested(Floor.ofLevel(2));
        elevator.doorStateChanged(DoorState.CLOSED);

        verify(engine).start(Direction.UP);
    }

    @Test
    public void should_ignore_not_requested_floors_when_multiple_floors_requested() {

        Elevator elevator = anElevator().build();

        elevator.floorRequested(Floor.ofLevel(4));
        elevator.floorRequested(Floor.ofLevel(2));
        elevator.doorStateChanged(DoorState.CLOSED);
        elevator.floorReached(Floor.ofLevel(1));

        verify(engine, times(0)).stop();
        verify(door, times(0)).open();
    }

    @Test
    @Parameters({"4, 2", "2, 4"})
    public void should_stop_at_nearest_requested_floor_when_multiple_floors_requested(
            int firstRequest, int secondRequest) {

        Elevator elevator = anElevator().build();

        elevator.floorRequested(Floor.ofLevel(firstRequest));
        elevator.floorRequested(Floor.ofLevel(secondRequest));
        elevator.doorStateChanged(DoorState.CLOSED);
        elevator.floorReached(Floor.ofLevel(1));
        elevator.floorReached(Floor.ofLevel(2));

        verify(engine).stop();
    }

    @Test
    @Parameters({"4, 2", "2, 4"})
    public void should_open_door_at_nearest_requested_floor_when_multiple_floors_requested(
            int firstRequest, int secondRequest) {

        Elevator elevator = anElevator().build();

        elevator.floorRequested(Floor.ofLevel(firstRequest));
        elevator.floorRequested(Floor.ofLevel(secondRequest));
        elevator.doorStateChanged(DoorState.CLOSED);
        elevator.floorReached(Floor.ofLevel(1));
        elevator.floorReached(Floor.ofLevel(2));
        elevator.engineStopped();

        verify(door).open();
    }

    @Test
    @Parameters({"4, 2", "2, 4"})
    public void should_close_door_at_nearest_requested_floor_when_multiple_floors_requested(
            int firstRequest, int secondRequest) {

        Elevator elevator = anElevator().build();

        elevator.floorRequested(Floor.ofLevel(firstRequest));
        elevator.floorRequested(Floor.ofLevel(secondRequest));
        elevator.doorStateChanged(DoorState.CLOSED);
        elevator.floorReached(Floor.ofLevel(1));
        elevator.floorReached(Floor.ofLevel(2));
        elevator.engineStopped();
        elevator.doorStateChanged(DoorState.OPENED);

        verify(door, times(2)).close();
    }

    @Test
    @Parameters({"4, 2", "2, 4"})
    public void should_start_engine_at_nearest_requested_floor_when_multiple_floors_requested(
            int firstRequest, int secondRequest) {

        Elevator elevator = anElevator().build();

        elevator.floorRequested(Floor.ofLevel(firstRequest));
        elevator.floorRequested(Floor.ofLevel(secondRequest));
        elevator.doorStateChanged(DoorState.CLOSED);
        elevator.floorReached(Floor.ofLevel(1));
        elevator.floorReached(Floor.ofLevel(2));
        elevator.engineStopped();
        elevator.doorStateChanged(DoorState.OPENED);
        elevator.doorStateChanged(DoorState.CLOSED);

        verify(engine, times(2)).start(Direction.UP);
    }

    @Test
    @Parameters({"4, 2", "2, 4"})
    public void should_stop_at_next_requested_floor_when_multiple_floors_requested(
            int firstRequest, int secondRequest) {

        Elevator elevator = anElevator().build();

        elevator.floorRequested(Floor.ofLevel(firstRequest));
        elevator.floorRequested(Floor.ofLevel(secondRequest));
        elevator.doorStateChanged(DoorState.CLOSED);
        elevator.floorReached(Floor.ofLevel(1));
        elevator.floorReached(Floor.ofLevel(2));
        elevator.engineStopped();
        elevator.doorStateChanged(DoorState.OPENED);
        elevator.doorStateChanged(DoorState.CLOSED);
        elevator.floorReached(Floor.ofLevel(3));
        elevator.floorReached(Floor.ofLevel(4));

        verify(engine, times(2)).stop();
    }

}
