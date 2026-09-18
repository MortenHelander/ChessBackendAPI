package app.gameengine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.is;

class PositionConverterTest {

    @Test
    void fromCoordinates(){
        int rankOutOfBounds = -1;
        int file = 5;
        Position shouldBeNull = PositionConverter.fromCoordinates(rankOutOfBounds, file);

        assertThat(shouldBeNull,  nullValue());

        int rank = 6;
        int fileOutOfBounds = 8;
        shouldBeNull = PositionConverter.fromCoordinates(rank, fileOutOfBounds);

        assertThat(shouldBeNull, nullValue());

        Position inBounds = PositionConverter.fromCoordinates(file, rank);

        assertThat(inBounds, is(Position.F7));
    }

}