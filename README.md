# java-filmorate
Template repository for Filmorate project.

### ER-diagram

![ER-diagram](img.png)

````
Table films {
  film_id int [primary key]
  name varchar(50) 
  description varchar(255)
  release_date date 
  duration int 
  rating_id int 
}

Table users {
  user_id int [primary key]
  email varchar(50) 
  login varchar(20) 
  name varchar(20)
  birthday date
}

Table likes {
  film_id int [primary key]
  user_id int [primary key]
}

Table genres {
  genre_id int [primary key]
  name varchar(20)
}

Table rating_mpa {
  rating_id int [primary key]
  name varchar(5)
}

Table film_genres {
  film_id int [primary key]
  genre_id int [primary key]
}

Table friends {
  user_id int [primary key]
  friend_id int [primary key]
  status bool
}

