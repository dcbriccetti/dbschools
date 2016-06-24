from django.contrib import admin
from .models import *


class StudentInline(admin.TabularInline):
    model = Student
    extra = 0


class ParentAdmin(admin.ModelAdmin):
    inlines = [StudentInline]

admin.site.register(Course)
admin.site.register(Section)
admin.site.register(Parent, ParentAdmin)
admin.site.register(Student)
