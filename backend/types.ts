/* eslint-disable */
// AUTO-GENERATED — DO NOT EDIT
// Run migrations to regenerate.

export type Json =
  | string
  | number
  | boolean
  | null
  | { [key: string]: Json | undefined }
  | Json[]

export type Database = {
  // Allows to automatically instantiate createClient with right options
  // instead of createClient<Database, { PostgrestVersion: 'XX' }>(URL, KEY)
  __InternalSupabase: {
    PostgrestVersion: "14.18"
  }
  public: {
    Tables: {
      fv_app_config: {
        Row: {
          data: Json
          key: string
          updated_at: string
        }
        Insert: {
          data: Json
          key: string
          updated_at?: string
        }
        Update: {
          data?: Json
          key?: string
          updated_at?: string
        }
        Relationships: []
      }
      fv_businesses: {
        Row: {
          created_at: string
          id: string
          name: string
        }
        Insert: {
          created_at?: string
          id: string
          name: string
        }
        Update: {
          created_at?: string
          id?: string
          name?: string
        }
        Relationships: []
      }
      fv_files: {
        Row: {
          author: string
          business_id: string
          created_at: string
          data: string
          id: string
        }
        Insert: {
          author?: string
          business_id: string
          created_at?: string
          data: string
          id: string
        }
        Update: {
          author?: string
          business_id?: string
          created_at?: string
          data?: string
          id?: string
        }
        Relationships: []
      }
      fv_login_fails: {
        Row: {
          count: number
          first_at: string
          username: string
        }
        Insert: {
          count: number
          first_at: string
          username: string
        }
        Update: {
          count?: number
          first_at?: string
          username?: string
        }
        Relationships: []
      }
      fv_records: {
        Row: {
          author: string
          business_id: string
          data: string
          deleted: boolean
          id: string
          kind: string
          seq: number
          updated_at: number
        }
        Insert: {
          author?: string
          business_id: string
          data: string
          deleted?: boolean
          id: string
          kind: string
          seq?: number
          updated_at: number
        }
        Update: {
          author?: string
          business_id?: string
          data?: string
          deleted?: boolean
          id?: string
          kind?: string
          seq?: number
          updated_at?: number
        }
        Relationships: [
          {
            foreignKeyName: "fv_records_business_id_fkey"
            columns: ["business_id"]
            isOneToOne: false
            referencedRelation: "fv_businesses"
            referencedColumns: ["id"]
          },
        ]
      }
      fv_recovery_fails: {
        Row: {
          count: number
          first_at: string
          username: string
        }
        Insert: {
          count?: number
          first_at?: string
          username: string
        }
        Update: {
          count?: number
          first_at?: string
          username?: string
        }
        Relationships: []
      }
      fv_register_hits: {
        Row: {
          at: string
          ip: string
        }
        Insert: {
          at?: string
          ip: string
        }
        Update: {
          at?: string
          ip?: string
        }
        Relationships: []
      }
      fv_sessions: {
        Row: {
          created_at: string
          token: string
          username: string
        }
        Insert: {
          created_at?: string
          token: string
          username: string
        }
        Update: {
          created_at?: string
          token?: string
          username?: string
        }
        Relationships: [
          {
            foreignKeyName: "fv_sessions_username_fkey"
            columns: ["username"]
            isOneToOne: false
            referencedRelation: "fv_users"
            referencedColumns: ["username"]
          },
        ]
      }
      fv_users: {
        Row: {
          business_id: string
          created_at: string
          display_name: string
          is_creator: boolean
          pass_hash: string
          role: string
          security_answer_hash: string | null
          security_question: string | null
          title: string
          username: string
        }
        Insert: {
          business_id: string
          created_at?: string
          display_name: string
          is_creator?: boolean
          pass_hash: string
          role: string
          security_answer_hash?: string | null
          security_question?: string | null
          title?: string
          username: string
        }
        Update: {
          business_id?: string
          created_at?: string
          display_name?: string
          is_creator?: boolean
          pass_hash?: string
          role?: string
          security_answer_hash?: string | null
          security_question?: string | null
          title?: string
          username?: string
        }
        Relationships: [
          {
            foreignKeyName: "fv_users_business_id_fkey"
            columns: ["business_id"]
            isOneToOne: false
            referencedRelation: "fv_businesses"
            referencedColumns: ["id"]
          },
        ]
      }
    }
    Views: {
      [_ in never]: never
    }
    Functions: {
      fv_account_delete: {
        Args: { p_password: string; p_token: string }
        Returns: Json
      }
      fv_credits_get: { Args: { p_token?: string }; Returns: Json }
      fv_credits_set: {
        Args: { p_credits: Json; p_token: string }
        Returns: Json
      }
      fv_file_get: { Args: { p_id: string; p_token: string }; Returns: Json }
      fv_file_put: {
        Args: { p_data: string; p_id: string; p_token: string }
        Returns: Json
      }
      fv_identity: {
        Args: { p_token: string }
        Returns: {
          business_id: string
          business_name: string
          display_name: string
          role: string
          username: string
        }[]
      }
      fv_is_void_only: {
        Args: { p_deleted: boolean; p_new: string; p_old: string }
        Returns: boolean
      }
      fv_login: {
        Args: { p_password: string; p_username: string }
        Returns: Json
      }
      fv_logout: { Args: { p_token: string }; Returns: Json }
      fv_norm_answer: { Args: { p: string }; Returns: string }
      fv_recover: {
        Args: { p_answer: string; p_new_password: string; p_username: string }
        Returns: Json
      }
      fv_recovery_question: { Args: { p_username: string }; Returns: Json }
      fv_register: {
        Args: {
          p_answer: string
          p_business_name: string
          p_display_name: string
          p_ip?: string
          p_password: string
          p_question: string
          p_username: string
        }
        Returns: Json
      }
      fv_security_get: { Args: { p_token: string }; Returns: Json }
      fv_security_set: {
        Args: {
          p_answer: string
          p_password: string
          p_question: string
          p_token: string
        }
        Returns: Json
      }
      fv_sync: {
        Args: { p_changes: Json; p_since: number; p_token: string }
        Returns: Json
      }
      fv_team_add:
        | {
            Args: {
              p_display_name: string
              p_password: string
              p_token: string
              p_username: string
            }
            Returns: Json
          }
        | {
            Args: {
              p_display_name: string
              p_password: string
              p_title: string
              p_token: string
              p_username: string
            }
            Returns: Json
          }
      fv_team_list: { Args: { p_token: string }; Returns: Json }
      fv_team_remove: {
        Args: { p_token: string; p_username: string }
        Returns: Json
      }
      fv_team_set_title: {
        Args: { p_title: string; p_token: string; p_username: string }
        Returns: Json
      }
      fv_verify: { Args: { p_token: string }; Returns: Json }
    }
    Enums: {
      [_ in never]: never
    }
    CompositeTypes: {
      [_ in never]: never
    }
  }
}

type DatabaseWithoutInternals = Omit<Database, "__InternalSupabase">

type DefaultSchema = DatabaseWithoutInternals[Extract<keyof Database, "public">]

export type Tables<
  DefaultSchemaTableNameOrOptions extends
    | keyof (DefaultSchema["Tables"] & DefaultSchema["Views"])
    | { schema: keyof DatabaseWithoutInternals },
  TableName extends (DefaultSchemaTableNameOrOptions extends {
    schema: keyof DatabaseWithoutInternals
  }
    ? keyof (DatabaseWithoutInternals[DefaultSchemaTableNameOrOptions["schema"]]["Tables"] &
        DatabaseWithoutInternals[DefaultSchemaTableNameOrOptions["schema"]]["Views"])
    : never) = never,
> = DefaultSchemaTableNameOrOptions extends {
  schema: keyof DatabaseWithoutInternals
}
  ? (DatabaseWithoutInternals[DefaultSchemaTableNameOrOptions["schema"]]["Tables"] &
      DatabaseWithoutInternals[DefaultSchemaTableNameOrOptions["schema"]]["Views"])[TableName] extends {
      Row: infer R
    }
    ? R
    : never
  : DefaultSchemaTableNameOrOptions extends keyof (DefaultSchema["Tables"] &
        DefaultSchema["Views"])
    ? (DefaultSchema["Tables"] &
        DefaultSchema["Views"])[DefaultSchemaTableNameOrOptions] extends {
        Row: infer R
      }
      ? R
      : never
    : never

export type TablesInsert<
  DefaultSchemaTableNameOrOptions extends
    | keyof DefaultSchema["Tables"]
    | { schema: keyof DatabaseWithoutInternals },
  TableName extends (DefaultSchemaTableNameOrOptions extends {
    schema: keyof DatabaseWithoutInternals
  }
    ? keyof DatabaseWithoutInternals[DefaultSchemaTableNameOrOptions["schema"]]["Tables"]
    : never) = never,
> = DefaultSchemaTableNameOrOptions extends {
  schema: keyof DatabaseWithoutInternals
}
  ? DatabaseWithoutInternals[DefaultSchemaTableNameOrOptions["schema"]]["Tables"][TableName] extends {
      Insert: infer I
    }
    ? I
    : never
  : DefaultSchemaTableNameOrOptions extends keyof DefaultSchema["Tables"]
    ? DefaultSchema["Tables"][DefaultSchemaTableNameOrOptions] extends {
        Insert: infer I
      }
      ? I
      : never
    : never

export type TablesUpdate<
  DefaultSchemaTableNameOrOptions extends
    | keyof DefaultSchema["Tables"]
    | { schema: keyof DatabaseWithoutInternals },
  TableName extends (DefaultSchemaTableNameOrOptions extends {
    schema: keyof DatabaseWithoutInternals
  }
    ? keyof DatabaseWithoutInternals[DefaultSchemaTableNameOrOptions["schema"]]["Tables"]
    : never) = never,
> = DefaultSchemaTableNameOrOptions extends {
  schema: keyof DatabaseWithoutInternals
}
  ? DatabaseWithoutInternals[DefaultSchemaTableNameOrOptions["schema"]]["Tables"][TableName] extends {
      Update: infer U
    }
    ? U
    : never
  : DefaultSchemaTableNameOrOptions extends keyof DefaultSchema["Tables"]
    ? DefaultSchema["Tables"][DefaultSchemaTableNameOrOptions] extends {
        Update: infer U
      }
      ? U
      : never
    : never

export type Enums<
  DefaultSchemaEnumNameOrOptions extends
    | keyof DefaultSchema["Enums"]
    | { schema: keyof DatabaseWithoutInternals },
  EnumName extends (DefaultSchemaEnumNameOrOptions extends {
    schema: keyof DatabaseWithoutInternals
  }
    ? keyof DatabaseWithoutInternals[DefaultSchemaEnumNameOrOptions["schema"]]["Enums"]
    : never) = never,
> = DefaultSchemaEnumNameOrOptions extends {
  schema: keyof DatabaseWithoutInternals
}
  ? DatabaseWithoutInternals[DefaultSchemaEnumNameOrOptions["schema"]]["Enums"][EnumName]
  : DefaultSchemaEnumNameOrOptions extends keyof DefaultSchema["Enums"]
    ? DefaultSchema["Enums"][DefaultSchemaEnumNameOrOptions]
    : never

export type CompositeTypes<
  PublicCompositeTypeNameOrOptions extends
    | keyof DefaultSchema["CompositeTypes"]
    | { schema: keyof DatabaseWithoutInternals },
  CompositeTypeName extends (PublicCompositeTypeNameOrOptions extends {
    schema: keyof DatabaseWithoutInternals
  }
    ? keyof DatabaseWithoutInternals[PublicCompositeTypeNameOrOptions["schema"]]["CompositeTypes"]
    : never) = never,
> = PublicCompositeTypeNameOrOptions extends {
  schema: keyof DatabaseWithoutInternals
}
  ? DatabaseWithoutInternals[PublicCompositeTypeNameOrOptions["schema"]]["CompositeTypes"][CompositeTypeName]
  : PublicCompositeTypeNameOrOptions extends keyof DefaultSchema["CompositeTypes"]
    ? DefaultSchema["CompositeTypes"][PublicCompositeTypeNameOrOptions]
    : never

export const Constants = {
  public: {
    Enums: {},
  },
} as const
