library(caret)

new_data <- read.csv("/home/otavio/analiseR/doutorado/master_data/ml_models/input.csv")
new_data <- new_data[1:4]
new_data <- scale(new_data)
final_model <- readRDS("/home/otavio/analiseR/doutorado/master_data/ml_models/final_model.rds")

predictions <- predict(final_model, new_data)
tmp <- read.csv("/home/otavio/analiseR/doutorado/master_data/ml_models/input.csv")
tmp <- cbind(tmp, predictions)
write.csv(tmp, "/home/otavio/analiseR/doutorado/master_data/ml_models/output.csv")