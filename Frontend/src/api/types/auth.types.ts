export interface UserLoginRequest {
  email: string;
  password: string;
}

export interface UserRegisterRequest {
  email: string;
  password: string;
  fullName: string;
  phone?: string; // Optional since it's not supported by backend yet
}

export interface AuthResponse {
  token: string;
}

export interface ApiResponse<T> {
  status: number;
  message: string;
  data?: T;
}
