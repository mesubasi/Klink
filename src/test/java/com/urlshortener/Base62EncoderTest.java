package com.urlshortener;

import com.urlshortener.util.Base62Encoder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Base62EncoderTest {

    @Test
    public void testEncodeZero() {
        assertEquals("0", Base62Encoder.encode(0));
    }

    @Test
    public void testEncodePositiveNumber() {
        String code = Base62Encoder.encode(125);
        assertNotNull(code);
        assertFalse(code.isEmpty());
    }

    @Test
    public void testGenerateRandomCodeLength() {
        String code = Base62Encoder.generateRandomCode(7);
        assertNotNull(code);
        assertEquals(7, code.length());
    }
}
