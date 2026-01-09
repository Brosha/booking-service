INSERT INTO hotels (id, name, address)
VALUES (1, 'Demo Hotel One', 'City Center 1');

INSERT INTO hotels (id, name, address)
VALUES (2, 'Demo Hotel Two', 'City Center 2');

INSERT INTO hotels (id, name, address)
VALUES (3, 'Airport Hotel', 'Airport Road 3');

-- Rooms for Hotel 1: mix of fresh and popular rooms
INSERT INTO rooms (id, hotel_id, number, available, times_booked, last_booking_id, hold_until)
VALUES (1, 1, '101', TRUE, 0, NULL, NULL);

INSERT INTO rooms (id, hotel_id, number, available, times_booked, last_booking_id, hold_until)
VALUES (2, 1, '102', TRUE, 5, NULL, NULL);

INSERT INTO rooms (id, hotel_id, number, available, times_booked, last_booking_id, hold_until)
VALUES (3, 1, '103', FALSE, 10, NULL, NULL);

-- Rooms for Hotel 2
INSERT INTO rooms (id, hotel_id, number, available, times_booked, last_booking_id, hold_until)
VALUES (4, 2, '201', TRUE, 1, NULL, NULL);

INSERT INTO rooms (id, hotel_id, number, available, times_booked, last_booking_id, hold_until)
VALUES (5, 2, '202', TRUE, 3, NULL, NULL);

-- Rooms for Hotel 3
INSERT INTO rooms (id, hotel_id, number, available, times_booked, last_booking_id, hold_until)
VALUES (6, 3, '301', TRUE, 0, NULL, NULL);
