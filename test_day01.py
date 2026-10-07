#!/usr/bin/env python3

def process_rotation(current_position, rotation):
    """Process a single rotation and return the new position and whether we landed on 0"""
    direction = rotation[0]
    distance = int(rotation[1:])
    
    if direction == 'L':
        # Moving left (toward lower numbers)
        raw = current_position - distance
        if raw >= 0:
            new_position = raw
        else:
            new_position = raw + 100
    else:  # direction == 'R'
        # Moving right (toward higher numbers)
        raw = current_position + distance
        if raw < 100:
            new_position = raw
        else:
            new_position = raw - 100
    
    landed_on_zero = (new_position == 0)
    return new_position, landed_on_zero

def part1(rotations):
    """Count how many times the dial points to 0 after any rotation in the sequence"""
    current_position = 50
    zero_count = 0
    
    for rotation in rotations:
        current_position, landed_on_zero = process_rotation(current_position, rotation)
        if landed_on_zero:
            zero_count += 1
    
    return zero_count

# Test the example from the problem
test_input = [
    "L68",
    "L30", 
    "R48",
    "L5",
    "R60",
    "L55",
    "L1",
    "L99",
    "R14",
    "L82"
]

print(f"Test input: {test_input}")
result = part1(test_input)
print(f"Result: {result} (expected: 3)")
print(f"Test passed: {result == 3}")

# Test individual rotations
print("\nTesting individual rotations:")
pos = 50
for rot in test_input:
    new_pos, landed_on_zero = process_rotation(pos, rot)
    print(f"Rot: {rot}, Pos: {pos} -> {new_pos}, Zero: {landed_on_zero}")
    pos = new_pos

# Now let's test with the actual input (first few lines)
print("\nTesting with actual input (first 10 lines):")
with open("/workspaces/scala3-aoc-2025/src/main/resources/inputs/Day01.txt", "r") as f:
    lines = [line.strip() for line in f.readlines()]
    
actual_input = lines[:10]
print(f"First 10 lines: {actual_input}")
result_actual = part1(actual_input)
print(f"Result for first 10 lines: {result_actual}")

# Let's also compute the full answer
print("\nComputing full answer:")
full_result = part1(lines)
print(f"Full result: {full_result}")