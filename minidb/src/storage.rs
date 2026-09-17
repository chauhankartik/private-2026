use std::fs::{File, OpenOptions};
use std::io::{self, Read, Seek, SeekFrom, Write};
use std::path::Path;

pub const USERNAME_MAX: usize = 32;
pub const EMAIL_MAX: usize = 255;
pub const ROW_SIZE: usize = 4 + USERNAME_MAX + EMAIL_MAX; // 291 bytes
pub const PAGE_SIZE: usize = 4096;
pub const ROWS_PER_PAGE: usize = PAGE_SIZE / ROW_SIZE;
pub const TABLE_MAX_PAGES: usize = 100;
pub const TABLE_MAX_ROWS: usize = ROWS_PER_PAGE * TABLE_MAX_PAGES;

#[derive(Debug, Clone, Copy)]
pub struct Row {
    pub id: u32,
    pub username: [u8; USERNAME_MAX],
    pub email: [u8; EMAIL_MAX],
}

impl Row {
    pub fn new(id: u32, username: &str, email: &str) -> Self {
        let mut r = Row {
            id,
            username: [0; USERNAME_MAX],
            email: [0; EMAIL_MAX],
        };
        let user_bytes = username.as_bytes();
        let email_bytes = email.as_bytes();
        let user_len = user_bytes.len().min(USERNAME_MAX);
        let email_len = email_bytes.len().min(EMAIL_MAX);
        r.username[..user_len].copy_from_slice(&user_bytes[..user_len]);
        r.email[..email_len].copy_from_slice(&email_bytes[..email_len]);
        r
    }

    pub fn to_bytes(&self) -> [u8; ROW_SIZE] {
        let mut bytes = [0u8; ROW_SIZE];
        bytes[0..4].copy_from_slice(&self.id.to_le_bytes());
        bytes[4..4+USERNAME_MAX].copy_from_slice(&self.username);
        bytes[4+USERNAME_MAX..ROW_SIZE].copy_from_slice(&self.email);
        bytes
    }

    pub fn from_bytes(bytes: &[u8]) -> Self {
        let mut id_bytes = [0u8; 4];
        id_bytes.copy_from_slice(&bytes[0..4]);
        let id = u32::from_le_bytes(id_bytes);

        let mut username = [0u8; USERNAME_MAX];
        username.copy_from_slice(&bytes[4..4+USERNAME_MAX]);

        let mut email = [0u8; EMAIL_MAX];
        email.copy_from_slice(&bytes[4+USERNAME_MAX..ROW_SIZE]);

        Row { id, username, email }
    }

    pub fn get_username(&self) -> String {
        let null_pos = self.username.iter().position(|&b| b == 0).unwrap_or(USERNAME_MAX);
        String::from_utf8_lossy(&self.username[..null_pos]).into_owned()
    }

    pub fn get_email(&self) -> String {
        let null_pos = self.email.iter().position(|&b| b == 0).unwrap_or(EMAIL_MAX);
        String::from_utf8_lossy(&self.email[..null_pos]).into_owned()
    }
}

pub struct Pager {
    file: File,
    pub file_length: u64,
    pub pages: Vec<Option<Vec<u8>>>,
}

impl Pager {
    pub fn open(filename: &str) -> io::Result<Self> {
        let path = Path::new(filename);
        let file = OpenOptions::new()
            .read(true)
            .write(true)
            .create(true)
            .open(path)?;

        let file_length = file.metadata()?.len();
        let mut pages = Vec::with_capacity(TABLE_MAX_PAGES);
        for _ in 0..TABLE_MAX_PAGES {
            pages.push(None);
        }

        Ok(Pager {
            file,
            file_length,
            pages,
        })
    }

    pub fn get_page(&mut self, page_num: usize) -> io::Result<&mut Vec<u8>> {
        if page_num >= TABLE_MAX_PAGES {
            return Err(io::Error::new(io::ErrorKind::InvalidInput, "Page out of bounds"));
        }

        if self.pages[page_num].is_none() {
            let mut page = vec![0u8; PAGE_SIZE];
            let num_pages = (self.file_length / PAGE_SIZE as u64) as usize;

            if page_num <= num_pages {
                self.file.seek(SeekFrom::Start((page_num * PAGE_SIZE) as u64))?;
                let mut bytes_read = 0;
                while bytes_read < PAGE_SIZE {
                    match self.file.read(&mut page[bytes_read..]) {
                        Ok(0) => break,
                        Ok(n) => bytes_read += n,
                        Err(ref e) if e.kind() == io::ErrorKind::Interrupted => continue,
                        Err(e) => return Err(e),
                    }
                }
            }
            self.pages[page_num] = Some(page);
        }

        Ok(self.pages[page_num].as_mut().unwrap())
    }

    pub fn flush(&mut self, page_num: usize, size: usize) -> io::Result<()> {
        if let Some(page) = &self.pages[page_num] {
            self.file.seek(SeekFrom::Start((page_num * PAGE_SIZE) as u64))?;
            self.file.write_all(&page[..size])?;
        }
        Ok(())
    }
}

pub struct Table {
    pub num_rows: usize,
    pub pager: Pager,
}

impl Table {
    pub fn db_open(filename: &str) -> io::Result<Self> {
        let pager = Pager::open(filename)?;
        let num_rows = (pager.file_length / ROW_SIZE as u64) as usize;
        
        Ok(Table { num_rows, pager })
    }

    pub fn db_close(&mut self) -> io::Result<()> {
        let num_full_pages = self.num_rows / ROWS_PER_PAGE;
        
        for i in 0..num_full_pages {
            if self.pager.pages[i].is_some() {
                self.pager.flush(i, PAGE_SIZE)?;
                self.pager.pages[i] = None;
            }
        }

        let num_additional_rows = self.num_rows % ROWS_PER_PAGE;
        if num_additional_rows > 0 {
            let page_num = num_full_pages;
            if self.pager.pages[page_num].is_some() {
                self.pager.flush(page_num, num_additional_rows * ROW_SIZE)?;
                self.pager.pages[page_num] = None;
            }
        }
        
        Ok(())
    }

    pub fn row_slot(&mut self, row_num: usize) -> io::Result<(&mut [u8], usize)> {
        let page_num = row_num / ROWS_PER_PAGE;
        let page = self.pager.get_page(page_num)?;
        let row_offset = row_num % ROWS_PER_PAGE;
        let byte_offset = row_offset * ROW_SIZE;
        Ok((page, byte_offset))
    }
}
