export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  username: string;
  password: string;
}

export interface JwtResponse {
  token: string;
  userId: string;
  username: string;
  email: string;
}

export interface User {
  id: string;
  email: string;
  username: string;
}
