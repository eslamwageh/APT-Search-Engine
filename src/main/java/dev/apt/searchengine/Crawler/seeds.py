import random

def process_file(file_path, new_file_path):
    # Read the file
    with open(file_path, 'r') as file:
        lines = file.readlines()

    # Remove empty lines
    lines = [line.strip() for line in lines if line.strip()]

    # Remove duplicate lines
    lines = list(set(lines))

    # Shuffle the lines
    random.shuffle(lines)

    # Write the processed lines to a new file
    print("here we go")
    with open(new_file_path, 'w') as file:
        file.write('\n'.join(lines))

# Usage example
file_name = 'new_seeds'
file_path = file_name + '.txt'
new_file_path = file_name + '_pre.txt'
process_file(file_path, new_file_path)