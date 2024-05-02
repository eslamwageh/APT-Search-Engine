#include <iostream>
#include <string>
#include <fstream>
#include <vector>
#include <algorithm>
#include <random>
#include <chrono>
#include <unordered_set>

std::vector<std::string> readFileLines(const std::string& fileName) {
    std::vector<std::string> lines;
    std::ifstream file(fileName);
    if (file.is_open()) {
        std::string line;
        while (std::getline(file, line)) {
            if (!line.empty()) {
                lines.push_back(line);
            }
        }
        file.close();
    }
    return lines;
}

void removeDuplicateLines(std::vector<std::string>& lines) {
    std::unordered_set<std::string> uniqueLines(lines.begin(), lines.end());
    lines.assign(uniqueLines.begin(), uniqueLines.end());
}

void shuffleLines(std::vector<std::string>& lines) {
    unsigned seed = std::chrono::system_clock::now().time_since_epoch().count();
    std::shuffle(lines.begin(), lines.end(), std::default_random_engine(seed));
}

void writeFileLines(const std::string& fileName, const std::vector<std::string>& lines) {
    std::ofstream file(fileName);
    if (file.is_open()) {
        for (const std::string& line : lines) {
            file << line << std::endl;
        }
        file.close();
    }
}

int main() {
    std::string inputFileName, outputFileName;
    std::cout << "Enter input file name: ";
    std::cin >> inputFileName;
    std::cout << "Enter output file name: ";
    std::cin >> outputFileName;

    std::vector<std::string> lines = readFileLines(inputFileName);
    removeDuplicateLines(lines);
    shuffleLines(lines);
    writeFileLines(outputFileName, lines);

    std::cout << "File processing complete." << std::endl;

    return 0;
}
