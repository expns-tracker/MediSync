export interface AllergyDto {
  id: number;
  name: string;
  code: string;
  category: string;
}

export interface AllergyCreateDto {
  name: string;
  code: string;
  category: string;
}
