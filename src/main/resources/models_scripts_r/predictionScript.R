library(caret)

args <- commandArgs(trailingOnly = TRUE)

input_file <- args[1]
output_file <- args[2]
rds_file <- args[3]

new_data <- read.csv(input_file)
new_data <- new_data[1:4]
new_data <- scale(new_data)

final_model <- readRDS(rds_file)

predictions <- predict(final_model, new_data)

tmp <- read.csv(input_file)
tmp <- cbind(tmp, predictions)

write.csv(tmp, output_file)